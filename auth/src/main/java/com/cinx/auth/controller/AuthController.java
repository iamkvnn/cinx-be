package com.cinx.auth.controller;

import com.cinx.auth.dto.*;
import com.cinx.auth.model.User;
import com.cinx.auth.service.auth.IAuthenticationService;
import com.cinx.auth.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;
    private final IAuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterDto registerDto) {
        User user = userService.createUser(registerDto);
        return ResponseEntity.ok(
                new ApiResponse(true, "User registered successfully", new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getGender()))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @RequestBody AuthRequestDto request
    ) {
        AuthResponse authResponse = authenticationService.authenticate(request);
        return ResponseEntity.ok(
                new ApiResponse(true, "User logged in successfully", authResponse)
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse authResponse = authenticationService.refreshToken(request.token());
        return ResponseEntity.ok(
                new ApiResponse(true, "Refresh token successfully", authResponse)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(
            @RequestBody VerifyOtpDto request
    ) {
        authenticationService.verifyOtp(request);
        return ResponseEntity.ok(
                new ApiResponse(true, "User verified successfully", null)
        );
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse> resendOtp(
            @RequestBody ResendOtpDto request
    ) {
        authenticationService.sendOtp(request.email());
        return ResponseEntity.ok(
                new ApiResponse(true, "Otp send successfully", null)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> forgetPassword(
            @RequestBody ForgetPasswordRequest request
    ) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(
                new ApiResponse(true, "User password reset successfully", null)
        );
    }
}
