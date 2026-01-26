package com.cinx.auth.dto;

public record ForgetPasswordRequest(String email, String otp, String newPassword) {
}
