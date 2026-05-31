package com.cinx.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CreateNotificationRequest(
        @Schema(example = "New System Update")
        String title,
        @Schema(example = "The system will be under maintenance at 12 PM.")
        String message,
        List<String> userIds // list of user ids to send the notification to. If empty, send to all users
) {
}
