package com.cinx.auth.service.user;

import com.cinx.auth.consts.Role;
import com.cinx.auth.dto.EmailRequest;
import com.cinx.auth.dto.RegisterDto;
import com.cinx.auth.dto.UpdateProifileDto;
import com.cinx.auth.exception.AlreadyExistException;
import com.cinx.auth.exception.NotFoundException;
import com.cinx.auth.model.User;
import com.cinx.auth.repository.UserRepository;
import com.cinx.auth.service.mail.EmailQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.cinx.auth.utils.OtpGenerator.generateOtp;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
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
    public User createUser(RegisterDto user) {
        if (userRepository.existsByEmail(user.email())) {
            throw new AlreadyExistException("User already exists with email: " + user.email());
        }

        String otp = generateOtp();
        emailQueueService.enqueue(new EmailRequest(user.email(), "Xác nhận đăng ký tài khoản", "Mã xác nhận của bạn là: " + otp));

        return userRepository.save(User
                .builder()
                .email(user.email())
                .name(user.name())
                .password(passwordEncoder.encode(user.password()))
                .role(Role.USER)
                .gender(user.gender())
                .otp(otp)
                .isVerified(false)
                .otpExpireAt(LocalDateTime.now().plusSeconds(90))
                .build());
    }

    @Override
    public User updateUser(String id, User user) {
        User existingUser = findById(id);
        existingUser.setEmail(user.getEmail() != null ? user.getEmail() : existingUser.getEmail());
        existingUser.setPassword(user.getPassword() != null ? user.getPassword() : existingUser.getPassword());
        existingUser.setName(user.getName() != null ? user.getName() : existingUser.getName());
        existingUser.setGender(user.getGender() != null ? user.getGender() : existingUser.getGender());
        existingUser.setOtp(user.getOtp() != null ? user.getOtp() : existingUser.getOtp());
        existingUser.setIsVerified(user.getIsVerified() != null ? user.getIsVerified() : existingUser.getIsVerified());
        existingUser.setOtpExpireAt(user.getOtpExpireAt() != null ? user.getOtpExpireAt() : existingUser.getOtpExpireAt());
        existingUser.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : existingUser.getAvatarUrl());
        return userRepository.save(existingUser);
    }

    @Override
    public User updateProfile(String id, UpdateProifileDto dto, MultipartFile avatar) {
        if (avatar != null) {
            try {
                Path uploadPath = Paths.get("uploads/avatars/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = avatar.getOriginalFilename();
                assert fileName != null;
                String extension = getFileExtension(fileName);
                fileName = UUID.randomUUID() + "." + extension;
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return updateUser(id, User.builder()
                        .name(dto.name())
                        .gender(dto.gender())
                        .avatarUrl("http://localhost:8888/api/v1/users/avatars/" + fileName)
                        .build());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        else {
            return updateUser(id, User.builder()
                    .name(dto.name())
                    .gender(dto.gender())
                    .build());
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.split("\\.")[fileName.split("\\.").length - 1];
    }
}
