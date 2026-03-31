package com.cinx.notification.messaging;

import com.cinx.notification.service.push.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningEventConsumer {

    private final PushNotificationService pushNotificationService;

    @RabbitListener(queues = "notification.learning.reminder.queue",
            containerFactory = "rabbitListenerContainerFactory")
    public void handleLearningReminder(Map<String, Object> event) {
        log.info("Received learning reminder event: {}", event);

        try {
            String userId = (String) event.get("userId");
            String title = (String) event.get("title");
            String message = (String) event.get("message");

            if (userId != null && title != null && message != null) {
                pushNotificationService.sendPushNotificationToUser(userId, title, message);
            } else {
                log.warn("Invalid event payload: {}", event);
            }
        } catch (Exception e) {
            log.error("Error processing learning reminder event", e);
        }
    }
}