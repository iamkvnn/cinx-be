package com.cinx.auth.dto;

import com.cinx.auth.consts.Gender;
import jakarta.validation.constraints.NotBlank;

public record UpdateProifileDto (
        @NotBlank(message = "Họ và tên không được để trống")
        String name,
        Gender gender
) {
}
