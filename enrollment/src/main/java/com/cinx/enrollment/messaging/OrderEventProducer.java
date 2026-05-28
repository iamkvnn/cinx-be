package com.cinx.enrollment.messaging;

import com.cinx.enrollment.messaging.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private static final String EXCHANGE = "order.events.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishOrderCreatedEvent(OrderEvent event) {
        System.out.println("Publishing OrderEvent: " + event);
        outboxEventPublisher.enqueue(
                event.getId() + "-CREATED",
                "Order",
                event.getId(),
                "OrderCreated",
                EXCHANGE,
                "order.order.created",
                event
        );
    }

    public void publishOrderCancelledEvent(OrderEvent event) {
        System.out.println("Publishing OrderEvent: " + event);
        outboxEventPublisher.enqueue(
                event.getId() + "-CANCELLED",
                "Order",
                event.getId(),
                "OrderCancelled",
                EXCHANGE,
                "order.order.cancelled",
                event
        );
    }
}
