package com.cinx.auth.service.auth;

import com.cinx.auth.dto.*;

public interface IAuthenticationService {
    void sendOtp(String email);
    void verifyOtp(VerifyOtpDto request);
    AuthResponse authenticate(AuthRequestDto request);
    TokenResponseDto generateTokens(JWTPayload payload);
    TokenResponseDto refreshToken(String refreshToken);
}
