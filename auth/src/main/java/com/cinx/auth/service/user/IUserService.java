package com.cinx.auth.service.user;

import com.cinx.auth.consts.Role;
import com.cinx.auth.dto.request.*;
import com.cinx.auth.dto.response.GoogleProfileResponse;
import com.cinx.auth.model.User;

public interface IUserService {
    User findById(String id);
    User findByEmail(String email);
    void createUser(RegisterRequest user);
    User findByGoogleProfile(GoogleProfileResponse profile);
    User createUserByGoogleProfile(GoogleProfileResponse profile, Role role);
    void banUser(String userId, BanUserRequest request);
    void unbanUser(String userId);
    void checkAndUnbanIfNeeded(User user);
    String generateOtp(String email);
    void verifyEmail(VerifyEmailRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(ChangePasswordRequest request);
}
