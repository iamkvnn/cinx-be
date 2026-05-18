package com.cinx.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @Email(message = "Email không hợp lệ")
        @NotBlank(message = "Email không được để trống")
        @Schema(example = "nguyenvana@gmail.com")
        String email,

        @NotBlank(message = "Mật khẩu cũ không được để trống")
        @Schema(example = "OldPassword123!")
        String oldPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Schema(example = "NewPassword123!")
        String newPassword
) {
}
