package com.cinx.notification.messaging.consumer;

import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.service.dispatch.INotificationDispatchService;
import com.cinx.notification.service.idempotency.IdempotencyService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationListener {

    private final INotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = "notification.user.queue", ackMode = "MANUAL",
            containerFactory = "rabbitListenerContainerFactory")
    public void handleUserEvent(Map<String, Object> payload, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
                                @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received user event, routingKey={}", routingKey);
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }

            NotificationContext ctx;
            if (routingKey != null && routingKey.startsWith("user.instructor.pending")) {
                ctx = NotificationContext.builder()
                        .channels(List.of("IN_APP", "PUSH"))
                        .inAppPayload(Map.of(
                                "userIds", payload.get("userIds"),
                                "title", payload.get("title"),
                                "message", payload.get("message")
                        ))
                        .pushPayload(Map.of(
                                "userIds", payload.get("userIds"),
                                "title", payload.get("title"),
                                "message", payload.get("message"),
                                "data", Map.of()
                        ))
                        .build();
            } else {
                ctx = NotificationContext.builder()
                        .channels(List.of("EMAIL"))
                        .emailPayload(payload)
                        .build();
            }

            dispatchService.dispatch(ctx);
            idempotencyService.markSuccess(messageId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing user event, routingKey={}", routingKey, e);
            nack(channel, tag);
        }
    }

    private void nack(Channel channel, long tag) {
        try {
            channel.basicNack(tag, false, false);
        } catch (Exception ex) {
            log.error("Error nacking message", ex);
        }
    }
}
