package com.cinx.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record JWTPayload(
        @Schema(example = "usr_123456")
        String userId, 
        @Schema(example = "STUDENT")
        String role
) {
}
