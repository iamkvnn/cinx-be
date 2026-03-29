package com.cinx.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @Email(message = "Email không hợp lệ")
        @NotBlank(message = "Email không được để trống")
        String email,
        @NotBlank(message = "Mã OTP không được để trống")
        String otp,
        @NotBlank(message = "Mật khẩu mới không được để trống")
        String newPassword) {
}
