package com.cinx.notification.messaging.consumer;

import com.cinx.notification.service.idempotency.IdempotencyService;
import com.cinx.notification.strategy.NotificationChannelStrategy;
import com.cinx.notification.strategy.NotificationFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationConsumer {

    private final NotificationFactory notificationFactory;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.push.queue", containerFactory = "rabbitListenerContainerFactory")
    public void consume(Message message) {
        try {
            String messageId = message.getMessageProperties().getMessageId();
            if (messageId != null && !idempotencyService.checkAndSave(messageId)) {
                log.info("Duplicate push message ignored: {}", messageId);
                return;
            }

            Map<String, Object> payload = objectMapper.readValue(message.getBody(), new TypeReference<Map<String, Object>>() {});
            log.info("Received push event: {}", payload);

            NotificationChannelStrategy strategy = notificationFactory.getStrategy("PUSH");
            strategy.send(payload);

        } catch (Exception e) {
            log.error("Failed to process push message", e);
            throw new RuntimeException("Message processing failed, will be retried", e);
        }
    }
}
