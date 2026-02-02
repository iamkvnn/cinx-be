package com.cinx.auth.service.user;

import com.cinx.auth.dto.request.*;
import com.cinx.auth.model.User;

public interface IUserService {
    User findById(String id);
    User findByEmail(String email);
    void createUser(RegisterRequest user);
    String generateOtp(String email);
    void verifyEmail(VerifyEmailRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(ChangePasswordRequest request);
    void changeEmail(ChangeEmailRequest request);
}
