package com.cinx.learning.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private static final String EXCHANGE        = "notification.send.exchange";
    private static final String EMAIL_ROUTE     = "notification.email.send";
    private static final String IN_APP_ROUTE    = "notification.in-app.send";
    private static final String PUSH_ROUTE      = "notification.push.send";

    private final RabbitTemplate rabbitTemplate;

    public void sendEmail(String to, String subject, String body) {
        Map<String, Object> payload = Map.of(
                "to",      to,
                "subject", subject,
                "body",    body
        );
        publish(EMAIL_ROUTE, payload);
        log.info("Email notification queued → {}", to);
    }

    public void sendInApp(List<String> userIds, String title, String message) {
        if (userIds == null || userIds.isEmpty()) return;
        Map<String, Object> payload = Map.of(
                "userIds", userIds,
                "title",   title,
                "message", message
        );
        publish(IN_APP_ROUTE, payload);
        log.info("In-app notification queued for {} user(s)", userIds.size());
    }

    public void sendPush(List<String> userIds, String title, String body, Map<String, String> data) {
        if (userIds == null || userIds.isEmpty()) return;
        
        // Push notification usually needs userIds, title, body, and optional data
        Map<String, Object> payload = Map.of(
                "userIds", userIds,
                "title", title,
                "body", body,
                "data", data != null ? data : Map.of()
        );
        
        publish(PUSH_ROUTE, payload);
        log.info("Push notification queued for {} user(s)", userIds.size());
    }

    private void publish(String routingKey, Map<String, Object> payload) {
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload, msg -> {
            msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            msg.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            return msg;
        });
    }
}
