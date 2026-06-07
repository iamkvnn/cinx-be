package com.cinx.auth.dto.request;

import com.cinx.auth.consts.DeviceType;
import com.cinx.auth.consts.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record OAuthRequest(
        @NotBlank
        @Schema(example = "4/0AeaYSHC...")
        String code,
        @NotBlank
        @Schema(example = "code_verifier_string...")
        String codeVerifier,
        @Schema(example = "WEB")
        DeviceType device,
        @Schema(example = "USER")
        Role role
) {
}
