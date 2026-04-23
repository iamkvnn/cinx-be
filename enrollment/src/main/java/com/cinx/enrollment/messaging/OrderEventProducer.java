package com.cinx.enrollment.messaging;

import com.cinx.enrollment.messaging.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreatedEvent(OrderEvent event) {
        System.out.println("Publishing OrderEvent: " + event);
        rabbitTemplate.convertAndSend("order.events.exchange", "order.order.created", event, m -> {
            m.getMessageProperties().setMessageId(event.getId() + "-CREATED");
            return m;
        });
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send", Map.of(
                        "userIds", List.of(event.getUserId()),
                        "title", "Order Created",
                        "message", "Your order with ID " + event.getId() + " has been created."
                ), m -> {
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
