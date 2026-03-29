package com.cinx.user.service.user;

import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
    Page<UserDto> findAll(int page, int size);
    UserDto findByEmail(String email);
    UserDto findByUserId(String userId);
    Boolean checkInstructorVerified(String id);
    UserDto createUser(CreateUserRequest user);
    void verifyInstructor(String id);

    UserDto updateUser(String id, UpdateProfileRequest dto, String avatarUrl);

    UserDto updateProfile(String id, UpdateProfileRequest dto, MultipartFile avatar);

    List<UserDto> findByIds(List<String> ids);
}
