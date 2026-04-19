package com.cinx.notification.strategy;

import com.cinx.notification.service.push.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationStrategy implements NotificationChannelStrategy {

    private final PushNotificationService pushNotificationService;

    @Override
    public void send(Map<String, Object> payload) {
        String userId = null;
        if (payload.containsKey("userId")) userId = String.valueOf(payload.get("userId"));
        else if (payload.containsKey("toUser")) userId = String.valueOf(payload.get("toUser"));

        String title = (String) payload.get("title");
        String message = (String) payload.get("message");

        if (userId == null || title == null || message == null) {
            log.error("Push payload missing required fields: {}", payload);
            throw new IllegalArgumentException("Push payload missing required fields: 'userId/toUser', 'title', 'message'");
        }

        try {
            pushNotificationService.sendPushNotificationToUser(userId, title, message);
            log.info("Sent push notification to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send push notification for user {}", userId, e);
            throw new RuntimeException("Failed to send push notification", e);
        }
    }

    @Override
    public String getChannelName() {
        return "PUSH";
    }
}
