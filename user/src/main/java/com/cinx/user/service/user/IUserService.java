package com.cinx.user.service.user;

import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProifileRequest;
import com.cinx.user.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserDto findById(String id);
    UserDto findByEmail(String email);
    UserDto findByUserId(String userId);
    void createUser(CreateUserRequest user);
    void updateUser(String id, UpdateProifileRequest dto);
    void updateProfile(String id, UpdateProifileRequest dto, MultipartFile avatar);
}
