package com.cinx.user.service.user;

import com.cinx.user.consts.Role;
import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.user.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IUserService {
    Page<UserDto> findAll(int page, int size, String query, Role role, Boolean isInstructorVerified, String sort);
    UserDto findByEmail(String email);
    UserDto findByUserId(String userId);
    Boolean checkInstructorVerified(String id);
    UserDto createUser(CreateUserRequest user);
    void verifyInstructor(String id);
    void rejectInstructor(String id, String reason);
    void terminatePartnership(String id);
    void toggleBan(String id);
    UserDto updateProfile(String id, UpdateProfileRequest dto);
    void updateLastAccess(String userId);

    List<UserDto> findByIds(List<String> ids);
    List<String> findAdminUserIds();
    
    void saveDeviceToken(String userId, com.cinx.user.dto.request.DeviceTokenRequest request);
    List<String> getUserTokens(String userId);
    void updatePreferredCategories(String userId, List<String> categoryIds);

    UserDto addXp(String userId, Integer xpAmount);

    long countTotalUsers();
    long countUsersBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
