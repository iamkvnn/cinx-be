package com.cinx.user.service.user;

import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProifileRequest;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.user.dto.UserDto;
import com.cinx.user.mapper.UserMapper;
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
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private User getOrThrowById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private User getOrThrowByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private User getOrThrowByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with userId: " + userId));
    }

    @Override
    public UserDto findById(String id) {
        return userMapper.toDto(getOrThrowById(id));
    }

    @Override
    public UserDto findByEmail(String email) {
        return userMapper.toDto(getOrThrowByEmail(email));
    }

    @Override
    public UserDto findByUserId(String userId) {
        return userMapper.toDto(getOrThrowByUserId(userId));
    }

    @Override
    public void createUser(CreateUserRequest user) {
        if (userRepository.existsByEmail(user.email())) {
            throw new AlreadyExistException("User already exists with email: " + user.email());
        }

        userRepository.save(User
                .builder()
                .userId(user.userId())
                .email(user.email())
                .name(user.name())
                .gender(user.gender())
                .build());
    }

    @Override
    public void updateUser(String id, UpdateProifileRequest dto) {
        User existingUser = getOrThrowByUserId(id);
        userMapper.partialUpdate(existingUser, dto);
        userRepository.save(existingUser);
    }

    @Override
    public void updateProfile(String id, UpdateProifileRequest dto, MultipartFile avatar) {
        if (Objects.nonNull(avatar) && !avatar.isEmpty()) {
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
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        updateUser(id, dto);
    }

    private String getFileExtension(String fileName) {
        return fileName.split("\\.")[fileName.split("\\.").length - 1];
    }
}
