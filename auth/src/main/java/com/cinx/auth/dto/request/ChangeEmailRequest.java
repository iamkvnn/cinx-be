package com.cinx.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(
        @Email(message = "Email cũ không hợp lệ")
        @NotBlank(message = "Email cũ không được để trống")
        String oldEmail,

        @NotBlank(message = "Mã OTP không được để trống")
        String otp,

        @Email(message = "Email mới không hợp lệ")
        @NotBlank(message = "Email mới không được để trống")
        String newEmail
) {
}
