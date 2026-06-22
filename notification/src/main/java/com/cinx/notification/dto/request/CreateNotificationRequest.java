package com.cinx.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

public record CreateNotificationRequest(
        @Schema(example = "New System Update")
        String title,
        @Schema(example = "The system will be under maintenance at 12 PM.")
        String message,
        @Schema(example = "COURSE_PUBLISHED")
        String type,
        @Schema(example = "course_123")
        String referenceId,
        @Schema(example = "/courses/course_123")
        String actionUrl,
        Map<String, Object> metadata,
        List<String> userIds // list of user ids to send the notification to. If empty, send to all users
) {
    public CreateNotificationRequest(String title, String message, List<String> userIds) {
        this(title, message, null, null, null, Map.of(), userIds);
    }
}
