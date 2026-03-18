package com.cinx.notification.dto.response;

public record UserNotificationResponse(
        String id,
        String userId,
        String title,
        String message,
        Boolean isRead
) {
}
