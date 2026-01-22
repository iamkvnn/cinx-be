package com.cinx.auth.service.user;

import com.cinx.auth.dto.RegisterDto;
import com.cinx.auth.model.User;

public interface IUserService {
    User findByEmail(String email);
    User createUser(RegisterDto user);
}
