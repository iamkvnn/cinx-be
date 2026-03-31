package com.cinx.notification.messaging;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.messaging.event.OrderEvent;
import com.cinx.notification.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderQueueConsumer {
    private final INotificationService notificationService;

    @RabbitListener(queues = "notification.order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveOrderMessage(OrderEvent orderEvent) {
        System.out.println("Received order message: " + orderEvent);
        notificationService.sendNotification(
                new CreateNotificationRequest(
                        "Order Confirmation",
                        "Your order with ID " + orderEvent.getId() + " has been created successfully.",
                        List.of(orderEvent.getUserId())
                )
        );
    }
}
