package com.cinx.user.service.user;

import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProifileRequest;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.user.model.User;
import com.cinx.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Override
    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("User not found with userId: " + userId));
    }

    @Override
    public User createUser(CreateUserRequest user) {
        if (userRepository.existsByEmail(user.email())) {
            throw new AlreadyExistException("User already exists with email: " + user.email());
        }

        return userRepository.save(User
                .builder()
                .userId(user.userId())
                .email(user.email())
                .name(user.name())
                .gender(user.gender())
                .build());
    }

    @Override
    public User updateUser(String id, User user) {
        User existingUser = findById(id);
        existingUser.setEmail(user.getEmail() != null ? user.getEmail() : existingUser.getEmail());
        existingUser.setName(user.getName() != null ? user.getName() : existingUser.getName());
        existingUser.setGender(user.getGender() != null ? user.getGender() : existingUser.getGender());
        existingUser.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : existingUser.getAvatarUrl());
        return userRepository.save(existingUser);
    }

    @Override
    public User updateProfile(String id, UpdateProifileRequest dto, MultipartFile avatar) {
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
                        .avatarUrl("http://localhost:8089/api/v1/users/avatars/" + fileName)
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
