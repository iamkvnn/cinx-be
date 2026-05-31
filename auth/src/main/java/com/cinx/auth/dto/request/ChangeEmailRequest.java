package com.cinx.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(
        @Email(message = "Email cũ không hợp lệ")
        @NotBlank(message = "Email cũ không được để trống")
        @Schema(example = "oldemail@gmail.com")
        String oldEmail,

        @NotBlank(message = "Mã OTP không được để trống")
        @Schema(example = "123456")
        String otp,

        @Email(message = "Email mới không hợp lệ")
        @NotBlank(message = "Email mới không được để trống")
        @Schema(example = "newemail@gmail.com")
        String newEmail
) {
}
