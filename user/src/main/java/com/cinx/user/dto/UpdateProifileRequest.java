package com.cinx.user.dto;

import com.cinx.user.consts.Gender;
import jakarta.validation.constraints.NotBlank;

public record UpdateProifileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        String name,
        Gender gender
) {
}
