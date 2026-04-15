package com.cinx.payment.messaging;

import com.cinx.payment.consts.OrderStatus;
import com.cinx.payment.messaging.event.OrderEvent;
import com.cinx.payment.service.payment.PaymentServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderQueueConsumer {
    private final PaymentServiceFactory paymentServiceFactory;

    @RabbitListener(queues = "payment.order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveOrderMessage(OrderEvent orderEvent) {
        System.out.println("Received order message: " + orderEvent);
//        if (orderEvent.getStatus() == OrderStatus.CANCELLED) {
//            paymentServiceFactory.getPaymentService(orderEvent.getPaymentMethod()).cancelPayment(orderEvent.getId());
//        }
    }
}
