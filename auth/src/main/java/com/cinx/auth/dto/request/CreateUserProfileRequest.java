package com.cinx.auth.dto.request;

import com.cinx.auth.consts.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        String userId,
        @NotBlank(message = "Họ và tên không được để trống")
        String name,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,
        Gender gender
) {
}
