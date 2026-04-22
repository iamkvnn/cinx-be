package com.cinx.auth.service.user;

import com.cinx.auth.consts.Role;
import com.cinx.auth.consts.UserStatus;
import com.cinx.auth.dto.request.*;
import com.cinx.auth.dto.response.GoogleProfileResponse;
import com.cinx.auth.model.User;
import com.cinx.auth.repository.UserRepository;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

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
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", dto.email(), "subject", "Mã Xác Nhận OTP", "body", "Mã xác nhận OTP của bạn là: " + otp), 
                m -> { m.getMessageProperties().setMessageId(java.util.UUID.randomUUID().toString()); return m; });

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
        user.setStatus(UserStatus.BANNED);
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", user.getEmail(), "subject", "Thông báo tài khoản bị khóa", "body", "Tài khoản của bạn đã bị khóa với lý do: " + request.reason()), 
                m -> { m.getMessageProperties().setMessageId(java.util.UUID.randomUUID().toString()); return m; });
        userProfileService.toggleBanUser(userId);
        return userRepository.save(user);
    }

    @Override
    public User unbanUser(String userId) {
        User user = findById(userId);
        user.setStatus(UserStatus.ACTIVE);
        rabbitTemplate.convertAndSend("auth.events.exchange", "auth.email.send", 
                Map.of("to", user.getEmail(), "subject", "Thông báo tài khoản được mở khóa", "body", "Tài khoản của bạn đã được mở khóa. Bạn có thể đăng nhập và sử dụng dịch vụ như bình thường."), 
                m -> { m.getMessageProperties().setMessageId(java.util.UUID.randomUUID().toString()); return m; });
        userProfileService.toggleBanUser(userId);
        return userRepository.save(user);
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
