package com.cinx.enrollment.messaging;

import com.cinx.enrollment.messaging.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreatedEvent(OrderEvent event) {
        System.out.println("Publishing OrderEvent: " + event);
        // The notification service subscribes to order.events.exchange / order.order.created
        // and handles in-app notification from there — no direct publish to notification exchange.
        rabbitTemplate.convertAndSend("order.events.exchange", "order.order.created", event, m -> {
            m.getMessageProperties().setMessageId(event.getId() + "-CREATED");
            return m;
        });
    }

    public void publishOrderCancelledEvent(OrderEvent event) {
        System.out.println("Publishing OrderEvent: " + event);
        rabbitTemplate.convertAndSend("order.events.exchange", "order.order.cancelled", event, m -> {
            m.getMessageProperties().setMessageId(event.getId() + "-CANCELLED");
            return m;
        });
    }
}
