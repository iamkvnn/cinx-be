package com.cinx.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BanUserRequest(
        @NotBlank
        String reason
) {
}
