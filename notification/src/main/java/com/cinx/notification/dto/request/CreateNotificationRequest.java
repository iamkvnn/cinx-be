package com.cinx.notification.dto.request;

import java.util.List;

public record CreateNotificationRequest(
        String title,
        String message,
        List<String> userIds
) {
}
