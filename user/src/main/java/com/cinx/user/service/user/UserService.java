package com.cinx.user.service.user;

import com.cinx.common.mapper.SortConverter;
import com.cinx.user.consts.Role;
import com.cinx.user.consts.UserStatus;
import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.user.dto.UserDto;
import com.cinx.user.dto.request.TerminatePartnershipRequest;
import com.cinx.user.mapper.UserMapper;
import com.cinx.user.messaging.UserEventProducer;
import com.cinx.user.model.User;
import com.cinx.user.model.UserDeviceToken;
import com.cinx.user.repository.UserRepository;
import com.cinx.user.repository.UserDeviceTokenRepository;
import com.cinx.user.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final UserEventProducer userEventProducer;
    private final UserMapper userMapper;
    private final S3Service s3Service;

    @Value("${aws.s3.cdn-url}")
    private String s3CdnUrl;

    private User getOrThrowByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private User getOrThrowByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with userId: " + userId));
    }

    @Override
    public Page<UserDto> findAll(int page, int size, String query, Role role, Boolean isInstructorVerified, String sort) {
        return userRepository.findAll(query, role, isInstructorVerified, PageRequest.of(page - 1, size, SortConverter.toSort(sort))).map(userMapper::toDto);
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
        User user = getOrThrowByUserId(id);
        if (Boolean.TRUE.equals(user.getIsPartnershipTerminated())) {
            return false;
        }
        return user.getIsInstructorVerified();
    }

    @Override
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "User already exists with email: " + request.email());
        }
        User user = userRepository.save(User
                .builder()
                .userId(request.userId())
                .email(request.email())
                .name(request.name())
                .role(request.role())
                .isInstructorVerified(false)
                .status(UserStatus.ACTIVE)
                .gender(request.gender())
                .phoneNumber(request.phoneNumber())
                .bio(request.bio())
                .cvFileKey(request.cvFileKey())
                .cvUrl(request.cvFileKey() != null ? s3CdnUrl + "/" + request.cvFileKey() : null)
                .build());
        // Only notify admins for new instructor registrations
        if (request.role() == Role.INSTRUCTOR) {
            List<String> adminUserIds = userRepository.findAllByRole(Role.ADMIN).stream()
                    .map(User::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            userEventProducer.sendNewInstructorNotification(user, adminUserIds);
        }
        return userMapper.toDto(user);
    }

    @Override
    public void verifyInstructor(String id) {
        User user = getOrThrowByUserId(id);
        user.setIsInstructorVerified(true);
        user.setInstructorVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        userEventProducer.sendInstructorVerifiedEmail(user);
    }

    @Override
    public void rejectInstructor(String id, String reason) {
        User user = getOrThrowByUserId(id);
        user.setIsInstructorVerified(false);
        userRepository.save(user);
        userEventProducer.sendInstructorRejectedEmail(user);
    }

    @Override
    @Transactional
    public void terminatePartnership(String id, TerminatePartnershipRequest request) {
        User user = getOrThrowByUserId(id);
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "User is not an instructor");
        }
        user.setIsInstructorVerified(false);
        user.setIsPartnershipTerminated(true);
        user.setPartnershipTerminatedAt(LocalDateTime.now());
        user.setPartnershipTerminationReasonType(request.reasonType());
        user.setPartnershipTerminationReasonDetail(normalizeReasonDetail(request.reasonDetail()));
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        userEventProducer.sendPartnershipTerminatedEmail(user);
    }

    private String normalizeReasonDetail(String reasonDetail) {
        if (reasonDetail == null || reasonDetail.isBlank()) {
            return null;
        }
        return reasonDetail.trim();
    }

    @Override
    public void toggleBan(String id) {
        User user = getOrThrowByUserId(id);
        if (user.getStatus() == null || user.getStatus() != UserStatus.BANNED) {
            user.setStatus(UserStatus.BANNED);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    @Override
    public UserDto updateProfile(String id, UpdateProfileRequest dto) {
        User existingUser = getOrThrowByUserId(id);
        
        if (dto.avatarFileKey() != null && !dto.avatarFileKey().equals(existingUser.getAvatarFileKey())) {
            if (existingUser.getAvatarFileKey() != null) {
                try {
                    s3Service.deleteObject(existingUser.getAvatarFileKey());
                } catch (Exception e) {
                    System.err.println("Error parsing/deleting S3 avatar URL: " + e.getMessage());
                }
            }
            existingUser.setAvatarUrl(s3CdnUrl + "/" + dto.avatarFileKey());
        }
        
        if (dto.cvFileKey() != null && !dto.cvFileKey().equals(existingUser.getCvFileKey())) {
            String oldCvUrl = existingUser.getCvUrl();
            if (oldCvUrl != null) {
                try {
                    s3Service.deleteObject(existingUser.getCvFileKey());
                } catch (Exception e) {
                    System.err.println("Error parsing/deleting S3 CV URL: " + e.getMessage());
                }
            }
            existingUser.setCvUrl(s3CdnUrl + "/" + dto.cvFileKey());
        }

        userMapper.partialUpdate(existingUser, dto);
        User user = userRepository.save(existingUser);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void updateLastAccess(String userId) {
        User user = getOrThrowByUserId(userId);
        user.setLastAccessAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserDto> findByIds(List<String> ids) {
        return userRepository.findAllByUserIdIn(ids).stream().map(userMapper::toDto).toList();
    }

    @Override
    public List<String> findAdminUserIds() {
        return userRepository.findAllByRole(Role.ADMIN).stream()
                .map(User::getUserId)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void saveDeviceToken(String userId, com.cinx.user.dto.request.DeviceTokenRequest request) {
        Optional<UserDeviceToken> existingToken = userDeviceTokenRepository.findByFcmToken(request.fcmToken());
        if (existingToken.isPresent()) {
            UserDeviceToken token = existingToken.get();
            if (!token.getUserId().equals(userId)) {
                // Token adopted by new user
                token.setUserId(userId);
                userDeviceTokenRepository.save(token);
            }
        } else {
            UserDeviceToken newToken = UserDeviceToken.builder()
                .userId(userId)
                .fcmToken(request.fcmToken())
                .deviceInfo(request.deviceInfo())
                .build();
            userDeviceTokenRepository.save(newToken);
        }
    }

    @Override
    public List<String> getUserTokens(String userId) {
        return userDeviceTokenRepository.findByUserId(userId).stream()
                .map(UserDeviceToken::getFcmToken)
                .toList();
    }

    @Override
    public void updatePreferredCategories(String userId, List<String> categoryIds) {
        getOrThrowByUserId(userId);
        List<String> normalizedCategoryIds = categoryIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(categoryId -> !categoryId.isBlank())
                .distinct()
                .toList();
        userEventProducer.publishPreferredCategoriesUpdated(userId, normalizedCategoryIds);
    }

    @Override
    @Transactional
    public UserDto addXp(String userId, Integer xpAmount) {
        User user = getOrThrowByUserId(userId);
        if (user.getXp() == null) {
            user.setXp(0);
        }
        user.setXp(user.getXp() + xpAmount);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public long countTotalUsers() {
        return userRepository.countTotalUsers();
    }

    @Override
    public long countUsersBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return userRepository.countUsersBetween(start, end);
    }
}
