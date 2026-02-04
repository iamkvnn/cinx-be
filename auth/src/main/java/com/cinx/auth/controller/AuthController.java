package com.cinx.auth.controller;

import com.cinx.auth.dto.request.*;
import com.cinx.auth.dto.response.TokenResponseDto;
import com.cinx.auth.service.auth.IAuthenticationService;
import com.cinx.auth.service.user.IUserService;
import com.cinx.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthenticationService authenticationService;
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.createUser(registerRequest);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User registered successfully", null)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody AuthRequestDto request
    ) {
        TokenResponseDto authResponse = authenticationService.authenticate(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User logged in successfully", authResponse)
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponseDto authResponse = authenticationService.refreshToken(request.token());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Refresh token successfully", authResponse)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        userService.verifyEmail(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User verified successfully", null)
        );
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<?>> resendOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        authenticationService.sendVerifyOtp(request.email());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Otp send successfully", null)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User password reset successfully", null)
        );
    }

    @PostMapping("/send-change-password-otp")
    public ResponseEntity<ApiResponse<?>> sendChangePasswordOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        authenticationService.sendChangePasswordOtp(request.email());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Otp for change password sent successfully", null)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User password changed successfully", null)
        );
    }

    @PostMapping("/send-change-email-otp")
    public ResponseEntity<ApiResponse<?>> sendChangeEmailOtp(
            @Valid @RequestBody SendOtpRequest request
    ) {
        authenticationService.sendChangeEmailOtp(request.email());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Otp for change email sent successfully", null)
        );
    }

    @PostMapping("/change-email")
    public ResponseEntity<ApiResponse<?>> changeEmail(
            @Valid @RequestBody ChangeEmailRequest request
    ) {
        userService.changeEmail(request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User email changed successfully", null)
        );
    }
}
