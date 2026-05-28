package com.cinx.payment.messaging;

import com.cinx.payment.messaging.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    private static final String EXCHANGE = "payment.events.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishPaymentSuccessEvent(PaymentEvent event) {
        System.out.println("Publishing payment success event: " + event);
        outboxEventPublisher.enqueue(
                event.getOrderId() + "-" + event.getStatus(),
                "Payment",
                event.getOrderId(),
                "Payment" + event.getStatus(),
                EXCHANGE,
                "payment.payment.success",
                event
        );
    }
}
