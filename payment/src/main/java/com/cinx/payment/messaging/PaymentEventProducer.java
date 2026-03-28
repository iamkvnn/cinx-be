package com.cinx.payment.messaging;

import com.cinx.payment.messaging.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentSuccessEvent(PaymentEvent event) {
        System.out.println("Publishing payment success event: " + event);
        rabbitTemplate.convertAndSend("payment.events.exchange", "payment.payment.success", event);
    }
}
