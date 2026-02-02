package com.cinx.auth.service.user;

import com.cinx.auth.consts.Role;
import com.cinx.auth.dto.request.*;
import com.cinx.auth.model.User;
import com.cinx.auth.repository.UserRepository;
import com.cinx.auth.service.mail.EmailQueueService;
import com.cinx.auth.service.userProfile.IUserProfileService;
import com.cinx.auth.utils.OtpGenerator;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final IUserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;
    private final EmailQueueService emailQueueService;

    @Override
    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Override
    public void createUser(RegisterRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new AlreadyExistException("User already exists with email: " + dto.email());
        }

        String otp = OtpGenerator.generateOtp();
        emailQueueService.enqueue(new EmailRequest(dto.email(), "Ma Xac Nhan OTP", otp));

        User user = userRepository.save(User
                .builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER)
                .isVerified(false)
                .otp(otp)
                .otpExpireAt(LocalDateTime.now().plusSeconds(90))
                .build());
        userProfileService.createUser(new CreateUserProfileRequest(user.getId(), dto.name(), dto.email(), dto.gender()));
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
        user.setIsVerified(true);
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
            throw new BadRequestException("Invalid password");
        }
        verifyOtp(user, request.otp());
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
