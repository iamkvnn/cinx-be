package com.cinx.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
    @NotBlank(message = "FCM token is required")
    @Schema(example = "fcm_token_xyz")
    String fcmToken,
    @Schema(example = "iPhone 13")
    String deviceInfo
) {}