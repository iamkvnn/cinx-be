package com.cinx.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponseDto(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken, 
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
}
