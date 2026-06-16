package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.order.OrderEvent;
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
        return NotificationContext.builder()
                .channels(List.of("IN_APP"))
                .inAppPayload(Map.of(
                        "userIds", List.of(event.getUserId()),
                        "title", "Order Created",
                        "message", "Your order with ID " + event.getId() + " has been created."
                ))
                .build();
    }

    private NotificationContext buildOrderCancelledContext(OrderEvent event) {
        UserDto user = getUser(event.getUserId());
        String formattedPrice = event.getTotalPrice() != null
                ? String.format("%,d VND", event.getTotalPrice())
                : "Free";

        String title = "Order Cancelled";
        String message = String.format(
                "Your order %s (%s) has been cancelled.",
                event.getId(), formattedPrice);

        return NotificationContext.builder()
                .channels(List.of("EMAIL", "IN_APP"))
                .emailPayload(Map.of(
                        "to", user.email(),
                        "subject", "Order Cancelled - Order " + event.getId(),
                        "body", String.format(
                                "<html><body><h2>Order Cancelled</h2>" +
                                        "<p>Dear %s,</p>" +
                                        "<p>Your order <b>%s</b> has been cancelled.</p>" +
                                        "<p>Total Amount: <b>%s</b></p>" +
                                        "</body></html>",
                                user.name(), event.getId(), formattedPrice)
                ))
                .inAppPayload(Map.of(
                        "userIds", List.of(event.getUserId()),
                        "title", title,
                        "message", message
                ))
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
