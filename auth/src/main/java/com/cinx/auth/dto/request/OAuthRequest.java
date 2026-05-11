package com.cinx.auth.dto.request;

import com.cinx.auth.consts.DeviceType;
import jakarta.validation.constraints.NotBlank;

public record OAuthRequest(
        @NotBlank
        String code,
        @NotBlank
        String codeVerifier,
        DeviceType device
) {
}
