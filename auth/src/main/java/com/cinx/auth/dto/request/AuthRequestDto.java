package com.cinx.auth.dto.request;

import com.cinx.auth.consts.Role;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record AuthRequestDto (
        @Email(message = "Email không hợp lệ")
        @NotBlank(message = "Email không được để trống")
        @Schema(example = "nguyenvana@gmail.com")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Schema(example = "Password123!")
        String password,

        @Schema(example = "USER")
        @NotNull(message = "Vai trò không được để trống")
        Role role
) {}
