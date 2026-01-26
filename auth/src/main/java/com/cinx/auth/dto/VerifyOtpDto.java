package com.cinx.auth.dto;

public record VerifyOtpDto (
    String email,
    String otp
) {
}
