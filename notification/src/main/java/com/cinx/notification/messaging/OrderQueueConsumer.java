package com.cinx.notification.messaging;

import com.cinx.notification.messaging.event.OrderEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderQueueConsumer {
    @RabbitListener(queues = "notification.order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveOrderMessage(OrderEvent orderEvent) {
        System.out.println("Received order message: " + orderEvent);
    }
}
