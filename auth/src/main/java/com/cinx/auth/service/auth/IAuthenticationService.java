package com.cinx.auth.service.auth;

import com.cinx.auth.dto.*;
import com.cinx.auth.dto.request.*;
import com.cinx.auth.dto.response.TokenResponseDto;

public interface IAuthenticationService {
    void sendVerifyOtp(String email);
    void sendForgotPasswordOtp(String email);
    void sendChangeEmailOtp(String email);
    TokenResponseDto authenticateWithGoogle(OAuthRequest request);
    TokenResponseDto authenticate(AuthRequestDto request);
    TokenResponseDto generateTokens(JWTPayload payload);
    TokenResponseDto refreshToken(String refreshToken);
}
