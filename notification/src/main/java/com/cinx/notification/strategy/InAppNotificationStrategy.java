package com.cinx.notification.strategy;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
        if (payload.containsKey("userIds")) userIds = payload.get("userIds") instanceof List<?> list ? list.stream().map(String::valueOf).toList() : null;

        String title = (String) payload.get("title");
        String message = (String) payload.get("message");
        String type = (String) payload.get("type");
        String referenceId = (String) payload.get("referenceId");
        String actionUrl = (String) payload.get("actionUrl");
        Map<String, Object> metadata = payload.get("metadata") instanceof Map<?, ?> map
                ? map.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()),
                        Map.Entry::getValue))
                : Map.of();

        if (userIds == null || title == null || message == null) {
            log.error("InApp payload missing required fields: {}", payload);
            throw new IllegalArgumentException("InApp payload missing required fields: 'userId/toUser', 'title', 'message'");
        }

        try {
            LocalDateTime sentAt = LocalDateTime.now();
            CreateNotificationRequest request = new CreateNotificationRequest(
                    title, message, type, referenceId, actionUrl, metadata, userIds);
            userIds.forEach(userId -> messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", Map.of(
                    "title", title,
                    "message", message,
                    "type", type == null ? "" : type,
                    "referenceId", referenceId == null ? "" : referenceId,
                    "actionUrl", actionUrl == null ? "" : actionUrl,
                    "metadata", metadata,
                    "sentAt", sentAt
            )));
            notificationService.sendNotification(request, sentAt);
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
