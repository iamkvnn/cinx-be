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
        rabbitTemplate.convertAndSend("order.events.exchange", "order.order.created", event);
    }
}
