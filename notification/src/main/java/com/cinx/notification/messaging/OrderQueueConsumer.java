package com.cinx.notification.messaging;

import com.cinx.notification.consts.OrderStatus;
import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.messaging.event.OrderEvent;
import com.cinx.notification.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderQueueConsumer {
    private final INotificationService notificationService;

    @RabbitListener(queues = "notification.order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveOrderMessage(OrderEvent orderEvent) {
        System.out.println("Received order message: " + orderEvent);
        String userId = orderEvent.getUserId();
        String orderId = orderEvent.getId();
        String title;
        String content;
        if (orderEvent.getStatus() == OrderStatus.PENDING) {
            title = "Order Created";
            content = "Your order with ID " + orderId + " has been created and is pending payment.";
        } else if (orderEvent.getStatus() == OrderStatus.CANCELLED) {
            title = "Order Cancelled";
            content = "Your order with ID " + orderId + " has been cancelled.";
        } else {
            title = "Order Completed";
            content = "Your order with ID " + orderId + " has been completed.";
        }
        CreateNotificationRequest request = new CreateNotificationRequest(title, content, List.of(userId));
        notificationService.sendNotification(request);
    }
}
