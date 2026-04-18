package com.cinx.auth.dto.request;

import com.cinx.auth.consts.Gender;
import com.cinx.auth.consts.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        String name,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,
        @NotBlank(message = "Mật khẩu không được để trống")
        String password,
        Role role,
        Gender gender,
        String cvUrl
) {
}
