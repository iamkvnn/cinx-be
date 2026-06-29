package com.cinx.auth.service.user;

import com.cinx.auth.consts.Role;
import com.cinx.auth.consts.UserStatus;
import com.cinx.auth.dto.request.*;
import com.cinx.auth.dto.response.GoogleProfileResponse;
import com.cinx.auth.model.User;
import com.cinx.auth.repository.UserRepository;
import com.cinx.auth.messaging.AuthNotificationPublisher;
import com.cinx.auth.service.userProfile.IUserProfileService;
import com.cinx.auth.utils.OtpGenerator;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final IUserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;
    private final AuthNotificationPublisher authNotificationPublisher;

    @Override
    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    @Override
    public void createUser(RegisterRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "User already exists with email: " + dto.email());
        }

        String otp = OtpGenerator.generateOtp();
        authNotificationPublisher.publishOtpVerifyEmail(dto.email(), otp);

        User user = userRepository.save(User
                .builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .status(UserStatus.UNVERIFIED)
                .otp(otp)
                .otpExpireAt(LocalDateTime.now().plusSeconds(90))
                .build());
        userProfileService.createUser(new CreateUserProfileRequest(
                user.getId(),
                dto.name(),
                dto.email(),
                dto.role(),
                dto.gender(),
                dto.phoneNumber(),
                dto.bio(),
                dto.cvFileKey()
        ));
    }

    @Override
    public User findByGoogleProfile(GoogleProfileResponse profile) {
        return userRepository.findByEmail(profile.email()).orElse(null);
    }

    @Transactional
    @Override
    public User createUserByGoogleProfile(GoogleProfileResponse profile, Role role) {
        User savedUser = userRepository.save(User.builder()
                .email(profile.email())
                .password(null)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build());
        userProfileService.createUser(new CreateUserProfileRequest(
                savedUser.getId(),
                profile.name(),
                profile.email(),
                role,
                null,
                null,
                null,
                null
        ));
        return savedUser;
    }

    @Override
    @Transactional
    public void banUser(String userId, BanUserRequest request) {
        User user = findById(userId);

        Integer duration = getDuration(request);

        user.setStatus(UserStatus.BANNED);
        if (duration != null) {
            user.setBanExpiresAt(LocalDateTime.now().plusDays(duration));
        } else {
            user.setBanExpiresAt(null);
        }

        userProfileService.toggleBanUser(userId);
        User savedUser = userRepository.save(user);

        String expirationText = duration != null ? "đến ngày " + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(savedUser.getBanExpiresAt()) : "vĩnh viễn";
        String body = String.format("Tài khoản của bạn đã bị khóa %s.\nLý do: %s\nChi tiết: %s", 
                expirationText, request.reasonType().name(), request.details());

        authNotificationPublisher.publishAccountBanned(user.getEmail(), body);
    }

    private Integer getDuration(BanUserRequest request) {
        Integer duration = request.durationDays();
        Integer maxDuration = request.reasonType().getMaxDurationDays();

        if (maxDuration != null) {
            if (duration == null) {
                throw new BadRequestException(ErrorCode.BAN_DURATION_REQUIRED, "Duration is required for reason type " + request.reasonType());
            }
            if (duration > maxDuration) {
                throw new BadRequestException(ErrorCode.BAN_DURATION_EXCEEDED, "Duration exceeds the maximum allowed (" + maxDuration + " days) for reason type " + request.reasonType());
            }
        }
        return duration;
    }

    @Override
    @Transactional
    public void unbanUser(String userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setBanExpiresAt(null);
        authNotificationPublisher.publishAccountUnbanned(user.getEmail());
        userProfileService.toggleBanUser(userId);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void checkAndUnbanIfNeeded(User user) {
        if (UserStatus.BANNED.equals(user.getStatus()) && user.getBanExpiresAt() != null && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
            user.setStatus(UserStatus.ACTIVE);
            user.setBanExpiresAt(null);
            userRepository.save(user);
            userProfileService.toggleBanUser(user.getId());
            authNotificationPublisher.publishAccountAutoUnbanned(user.getEmail());
        }
    }

    @Override
    @Transactional
    public String generateOtp(String email) {
        User user = findByEmail(email);
        String otp = OtpGenerator.generateOtp();
        user.setOtp(otp);
        user.setOtpExpireAt(LocalDateTime.now().plusSeconds(90));
        userRepository.save(user);
        return otp;
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = findByEmail(request.email());
        verifyOtp(user, request.otp());
        user.setStatus(UserStatus.ACTIVE);
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = findByEmail(request.email());
        verifyOtp(user, request.otp());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = findByEmail(request.email());
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorCode.INVALID_OLD_PASSWORD, "Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    private void verifyOtp(User user, String otp) {
        if (Objects.isNull(user.getOtp()) || !user.getOtp().equals(otp)) {
            throw new BadRequestException(ErrorCode.INVALID_OTP, "Invalid OTP");
        }
        if (user.getOtpExpireAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorCode.OTP_EXPIRED, "OTP has expired");
        }
    }
}
