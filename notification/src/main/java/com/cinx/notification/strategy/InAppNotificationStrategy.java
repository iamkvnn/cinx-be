package com.cinx.notification.strategy;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationStrategy implements NotificationChannelStrategy {

    private final INotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void send(Map<String, Object> payload) {
        List<String> userIds = null;
        if (payload.containsKey("userIds")) userIds = String.valueOf(payload.get("userIds")).lines().toList();
        else if (payload.containsKey("toUsers")) userIds = String.valueOf(payload.get("toUsers")).lines().toList();

        String title = (String) payload.get("title");
        String message = (String) payload.get("message");

        if (userIds == null || title == null || message == null) {
            log.error("InApp payload missing required fields: {}", payload);
            throw new IllegalArgumentException("InApp payload missing required fields: 'userId/toUser', 'title', 'message'");
        }

        try {
            CreateNotificationRequest request = new CreateNotificationRequest(title, message, userIds);
            userIds.forEach(userId -> messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", Map.of(
                    "title", title,
                    "message", message
            )));
            notificationService.sendNotification(request);
            log.info("Saved in-app notification for user {}", userIds);
        } catch (Exception e) {
            log.error("Failed to save in-app notification for user {}", userIds, e);
            throw new RuntimeException("Failed to save in-app notification", e);
        }
    }

    @Override
    public String getChannelName() {
        return "IN_APP";
    }
}
