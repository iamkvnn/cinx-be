package com.cinx.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserNotificationResponse(
        @Schema(example = "noti_123")
        String id,
        @Schema(example = "user_123")
        String userId,
        @Schema(example = "Course Enrolled Successfully")
        String title,
        @Schema(example = "You have successfully enrolled in Introduction to Java.")
        String message,
        @Schema(example = "false")
        Boolean isRead
) {
}
