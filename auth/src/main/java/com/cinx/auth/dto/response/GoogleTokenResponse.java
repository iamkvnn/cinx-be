package com.cinx.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record GoogleTokenResponse(
        @JsonProperty("access_token")
        @Schema(example = "ya29.a0AfB_by...")
        String accessToken,
        @JsonProperty("token_type")
        @Schema(example = "Bearer")
        String tokenType,
        @JsonProperty("expires_in")
        @Schema(example = "3599")
        Long expiresIn,
        @JsonProperty("refresh_token")
        @Schema(example = "1//0...")
        String refreshToken,
        @Schema(example = "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email")
        String scope
) {
}
