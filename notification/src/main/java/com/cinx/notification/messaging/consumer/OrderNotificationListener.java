package com.cinx.notification.messaging.consumer;

import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.order.OrderEvent;
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
public class OrderNotificationListener {

    private final INotificationDispatchService dispatchService;

    @RabbitListener(queues = "notification.order.queue", ackMode = "MANUAL")
    public void handleOrderCreated(OrderEvent event, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received order created event for orderId={}, userId={}", event.getId(), event.getUserId());
        try {
            NotificationContext ctx = NotificationContext.builder()
                    .channels(List.of("IN_APP"))
                    .inAppPayload(Map.of(
                            "userIds", List.of(event.getUserId()),
                            "title", "Order Created",
                            "message", "Your order with ID " + event.getId() + " has been created."
                    ))
                    .build();
            dispatchService.dispatch(ctx);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing order created event", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }
}
