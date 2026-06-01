package com.cinx.auth.dto.request;

import com.cinx.auth.consts.Gender;
import com.cinx.auth.consts.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Schema(example = "Nguyen Van A")
        String name,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Schema(example = "nguyenvana@gmail.com")
        String email,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Schema(example = "Password123!")
        String password,
        @Schema(example = "STUDENT")
        Role role,
        @Schema(example = "MALE")
        Gender gender,
        @Schema(example = "0987654321")
        String phoneNumber,
        @Schema(example = "Senior Frontend Developer")
        String bio,
        @Schema(example = "829df83c9-cv.pdf")
        String cvFileKey
) {
}
