package com.cinx.payment.messaging;

import com.cinx.payment.consts.OrderStatus;
import com.cinx.payment.messaging.event.OrderEvent;
import com.cinx.payment.service.payment.IPaymentStrategyService;
import com.cinx.payment.service.payment.PaymentServiceFactory;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderQueueConsumer {
    private final PaymentServiceFactory paymentServiceFactory;

    @RabbitListener(queues = "payment.order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveOrderMessage(OrderEvent orderEvent,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received order event: {}", orderEvent);
        try {
            IPaymentStrategyService paymentService = paymentServiceFactory.getPaymentService(orderEvent.getPaymentMethod());
            if (orderEvent.getStatus() == OrderStatus.CANCELLED) {
                paymentService.cancelPayment(null, orderEvent.getId());
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process order event orderId={}", orderEvent.getId(), e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception nackError) {
                log.error("Failed to nack order event", nackError);
            }
        }
    }

}
