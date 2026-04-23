package com.cinx.notification.strategy;

import com.cinx.notification.service.push.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationStrategy implements NotificationChannelStrategy {

    private final PushNotificationService pushNotificationService;

    @Override
    public void send(Map<String, Object> payload) {
        List<String> userIds = null;
        if (payload.containsKey("userId")) userIds = String.valueOf(payload.get("userIds")).lines().toList();
        else if (payload.containsKey("toUser")) userIds = String.valueOf(payload.get("toUsers")).lines().toList();

        String title = (String) payload.get("title");
        String message = (String) payload.get("message");

        if (userIds == null || title == null || message == null) {
            log.error("Push payload missing required fields: {}", payload);
            throw new IllegalArgumentException("Push payload missing required fields: 'userId/toUser', 'title', 'message'");
        }

        try {
            userIds.forEach(userId -> pushNotificationService.sendPushNotificationToUser(userId, title, message));
            log.info("Sent push notification to user {}", userIds);
        } catch (Exception e) {
            log.error("Failed to send push notification for user {}", userIds, e);
            throw new RuntimeException("Failed to send push notification", e);
        }
    }

    @Override
    public String getChannelName() {
        return "PUSH";
    }
}
