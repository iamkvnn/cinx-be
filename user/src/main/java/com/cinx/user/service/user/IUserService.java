package com.cinx.user.service.user;

import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UpdateProifileRequest;
import com.cinx.user.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    User findById(String id);
    User findByEmail(String email);
    User findByUserId(String userId);
    User createUser(CreateUserRequest user);
    User updateUser(String id, User user);
    User updateProfile(String id, UpdateProifileRequest dto, MultipartFile avatar);
}
