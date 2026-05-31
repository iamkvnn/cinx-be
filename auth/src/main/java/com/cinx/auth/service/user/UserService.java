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
            throw new AlreadyExistException("User already exists with email: " + dto.email());
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
        userProfileService.createUser(new CreateUserProfileRequest(user.getId(), dto.name(), dto.email(), dto.role(), dto.gender(), dto.cvFileKey()));
    }

    @Transactional
    @Override
    public User findOrCreateUserByGoogleProfile(GoogleProfileResponse profile) {
        return userRepository.findByEmail(profile.email())
                .orElseGet(() -> {
                    User savedUser = userRepository.save(User.builder()
                            .email(profile.email())
                            .password(null)
                            .role(Role.USER)
                            .status(UserStatus.ACTIVE)
                            .build());
                    userProfileService.createUser(new CreateUserProfileRequest(savedUser.getId(), profile.name(), profile.email(), Role.USER, null, null));
                    return savedUser;
                });
    }

    @Override
    public User banUser(String userId, BanUserRequest request) {
        User user = findById(userId);

        Integer duration = request.durationDays();
        Integer maxDuration = request.reasonType().getMaxDurationDays();

        if (maxDuration != null) {
            if (duration == null) {
                throw new BadRequestException("Duration is required for reason type " + request.reasonType());
            }
            if (duration > maxDuration) {
                throw new BadRequestException("Duration exceeds the maximum allowed (" + maxDuration + " days) for reason type " + request.reasonType());
            }
        }

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
        return savedUser;
    }

    @Override
    public User unbanUser(String userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.ACTIVE);
        user.setBanExpiresAt(null);
        authNotificationPublisher.publishAccountUnbanned(user.getEmail());
        userProfileService.toggleBanUser(userId);
        return userRepository.save(user);
    }

    @Override
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
    public String generateOtp(String email) {
        User user = findByEmail(email);
        String otp = OtpGenerator.generateOtp();
        user.setOtp(otp);
        user.setOtpExpireAt(LocalDateTime.now().plusSeconds(90));
        userRepository.save(user);
        return otp;
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        User user = findByEmail(request.email());
        verifyOtp(user, request.otp());
        user.setStatus(UserStatus.ACTIVE);
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = findByEmail(request.email());
        verifyOtp(user, request.otp());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = findByEmail(request.email());
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    @Override
    public void changeEmail(ChangeEmailRequest request) {
        User user = findByEmail(request.oldEmail());
        verifyOtp(user, request.otp());
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new AlreadyExistException("User already exists with email: " + request.newEmail());
        }
        user.setEmail(request.newEmail());
        user.setOtp(null);
        user.setOtpExpireAt(null);
        userRepository.save(user);
    }

    private void verifyOtp(User user, String otp) {
        if (Objects.isNull(user.getOtp()) || !user.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }
        if (user.getOtpExpireAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }
    }
}
