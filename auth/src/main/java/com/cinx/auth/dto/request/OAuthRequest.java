package com.cinx.auth.dto.request;

import com.cinx.auth.consts.DeviceType;

public record OAuthRequest(
        String code,
        String codeVerifier,
        DeviceType device
) {
}
