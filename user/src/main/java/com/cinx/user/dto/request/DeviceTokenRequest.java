package com.cinx.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
    @NotBlank(message = "FCM token is required")
    String fcmToken,
    String deviceInfo
) {}