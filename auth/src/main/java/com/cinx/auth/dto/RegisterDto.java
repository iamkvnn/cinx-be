package com.cinx.auth.dto;

import com.cinx.auth.consts.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterDto (
        @NotBlank(message = "Họ và tên không được để trống")
        String name,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,
        @NotBlank(message = "Mật khẩu không được để trống")
        String password,
        Gender gender
) {
}
