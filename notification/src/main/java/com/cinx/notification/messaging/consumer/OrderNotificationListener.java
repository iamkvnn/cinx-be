package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.order.OrderEvent;
import com.cinx.notification.service.format.NotificationFormatter;
import com.cinx.notification.service.format.NotificationMessage;
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
public class OrderNotificationListener {

    private final INotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;
    private final UserClient userClient;
    private final NotificationFormatter notificationFormatter;

    @RabbitListener(queues = "notification.order.queue", ackMode = "MANUAL")
    public void handleOrderCreated(OrderEvent event, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                   @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
                                   @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received order event routingKey={}, orderId={}, userId={}", routingKey, event.getId(), event.getUserId());
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }
            NotificationContext ctx = "order.order.cancelled".equals(routingKey)
                    ? buildOrderCancelledContext(event)
                    : buildOrderCreatedContext(event);
            dispatchService.dispatch(ctx);
            idempotencyService.markSuccess(messageId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing order event", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }

    private NotificationContext buildOrderCreatedContext(OrderEvent event) {
        NotificationMessage notification = notificationFormatter.orderCreated(event.getId());
        return NotificationContext.builder()
                .channels(List.of("IN_APP"))
                .inAppPayload(notification.inAppPayload(List.of(event.getUserId())))
                .build();
    }

    private NotificationContext buildOrderCancelledContext(OrderEvent event) {
        UserDto user = getUser(event.getUserId());
        String formattedPrice = event.getTotalPrice() != null
                ? String.format("%,d VND", event.getTotalPrice())
                : "0 VND";
        NotificationMessage notification = notificationFormatter.orderCancelled(
                event.getId(), formattedPrice, user.name());

        return NotificationContext.builder()
                .channels(List.of("EMAIL", "IN_APP"))
                .emailPayload(notification.emailPayload(user.email()))
                .inAppPayload(notification.inAppPayload(List.of(event.getUserId())))
                .build();
    }

    private UserDto getUser(String userId) {
        ApiResponse<UserDto> userResponse = userClient.getUserById(userId);
        if (userResponse == null || !userResponse.success() || userResponse.data() == null) {
            throw new IllegalStateException("Failed to fetch user details for userId: " + userId);
        }
        return userResponse.data();
    }
}
