package com.cinx.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpDto(
        @Email(message = "Email không hợp lệ")
        @NotBlank(message = "Email không được để trống")
        String email
) {
}
