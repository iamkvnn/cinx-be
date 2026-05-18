package com.cinx.notification.messaging.consumer;

import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.service.dispatch.INotificationDispatchService;
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
public class AuthNotificationListener {

    private final INotificationDispatchService dispatchService;

    @RabbitListener(queues = "notification.auth.queue", ackMode = "MANUAL",
            containerFactory = "rabbitListenerContainerFactory")
    public void handleAuthEvent(Map<String, Object> payload, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        log.info("Received auth event, routingKey={}", routingKey);
        try {
            // All auth events carry: { "to", "subject", "body" } → EMAIL channel
            NotificationContext ctx = NotificationContext.builder()
                    .channels(List.of("EMAIL"))
                    .emailPayload(payload)
                    .build();
            dispatchService.dispatch(ctx);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing auth event, routingKey={}", routingKey, e);
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
