package com.cinx.user.service.user;

import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.user.dto.UserDto;
import com.cinx.user.mapper.UserMapper;
import com.cinx.user.model.User;
import com.cinx.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private User getOrThrowByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private User getOrThrowByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with userId: " + userId));
    }

    @Override
    public Page<UserDto> findAll(int page, int size) {
        return userRepository.findAll(PageRequest.of(page - 1, size)).map(userMapper::toDto);
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
    public Boolean checkInstructorVerified(String id) {
        return getOrThrowByUserId(id).getIsInstructorVerified();
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistException("User already exists with email: " + request.email());
        }
        User user = userRepository.save(User
                .builder()
                .userId(request.userId())
                .email(request.email())
                .name(request.name())
                .role(request.role())
                .isInstructorVerified(false)
                .gender(request.gender())
                .build());
        return userMapper.toDto(user);
    }

    @Override
    public void verifyInstructor(String id) {
        User user = getOrThrowByUserId(id);
        user.setIsInstructorVerified(true);
        userRepository.save(user);
    }

    @Override
    public UserDto updateUser(String id, UpdateProfileRequest dto, String avatarUrl) {
        User existingUser = getOrThrowByUserId(id);
        userMapper.partialUpdate(existingUser, dto);
        if (avatarUrl != null) {
            existingUser.setAvatarUrl(avatarUrl);
        }
        User user = userRepository.save(existingUser);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateProfile(String id, UpdateProfileRequest dto, MultipartFile avatar) {
        String fileName = null;
        if (Objects.nonNull(avatar) && !avatar.isEmpty()) {
            try {
                Path uploadPath = Paths.get("uploads/avatars/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                fileName = avatar.getOriginalFilename();
                assert fileName != null;
                String extension = getFileExtension(fileName);
                fileName = UUID.randomUUID() + "." + extension;
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        String avatarUrl = fileName != null ? "http://localhost:9090/api/v1/users/avatars/" + fileName : null;
        return updateUser(id, dto, avatarUrl);
    }

    @Override
    public List<UserDto> findByIds(List<String> ids) {
        return userRepository.findAllByUserIdIn(ids).stream().map(userMapper::toDto).toList();
    }

    private String getFileExtension(String fileName) {
        return fileName.split("\\.")[fileName.split("\\.").length - 1];
    }
}
