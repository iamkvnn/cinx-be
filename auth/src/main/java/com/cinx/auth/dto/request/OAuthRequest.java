package com.cinx.auth.dto.request;

import com.cinx.auth.consts.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthRequest(
        @NotBlank
        @Schema(example = "4/0AeaYSHC...")
        String code,
        @NotBlank
        @Schema(example = "code_verifier_string...")
        String codeVerifier,
        @Schema(example = "USER")
        @NotNull
        Role role,
        @Schema(example = "http://localhost:3000")
        @NotBlank
        String redirectUri
) {
}
