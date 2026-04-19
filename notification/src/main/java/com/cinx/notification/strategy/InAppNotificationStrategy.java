package com.cinx.notification.strategy;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationStrategy implements NotificationChannelStrategy {

    private final INotificationService notificationService;

    @Override
    public void send(Map<String, Object> payload) {
        String userId = null;
        if (payload.containsKey("userId")) userId = String.valueOf(payload.get("userId"));
        else if (payload.containsKey("toUser")) userId = String.valueOf(payload.get("toUser"));

        String title = (String) payload.get("title");
        String message = (String) payload.get("message");

        if (userId == null || title == null || message == null) {
            log.error("InApp payload missing required fields: {}", payload);
            throw new IllegalArgumentException("InApp payload missing required fields: 'userId/toUser', 'title', 'message'");
        }

        try {
            CreateNotificationRequest request = new CreateNotificationRequest(title, message, List.of(userId));
            notificationService.sendNotification(request);
            log.info("Saved in-app notification for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to save in-app notification for user {}", userId, e);
            throw new RuntimeException("Failed to save in-app notification", e);
        }
    }

    @Override
    public String getChannelName() {
        return "IN_APP";
    }
}
