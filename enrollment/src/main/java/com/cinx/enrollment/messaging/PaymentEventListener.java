package com.cinx.enrollment.messaging;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.consts.PaymentStatus;
import com.cinx.enrollment.dto.request.CreateEnrolledCourseRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.messaging.event.PaymentEvent;
import com.cinx.enrollment.service.enrollment.IEnrollmentService;
import com.cinx.enrollment.service.order.IOrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final IEnrollmentService enrollmentService;
    private final IOrderService orderService;

    @RabbitListener(queues = "enrollment.payment.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receivePaymentMessage(PaymentEvent paymentEvent,
                                      Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received payment event: {}", paymentEvent);
        try {
            if (paymentEvent.getStatus() != PaymentStatus.PAID) {
                channel.basicAck(tag, false);
                return;
            }

            OrderDetailResponse orderDetail = orderService.updateOrderStatus(paymentEvent.getOrderId(), OrderStatus.PAID);
            enrollmentService.enrollCourses(orderDetail.items().stream()
                    .map(item -> new CreateEnrolledCourseRequest(item.courseId(), orderDetail.userId()))
                    .toList());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process payment event orderId={}", paymentEvent.getOrderId(), e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception nackError) {
                log.error("Failed to nack payment event", nackError);
            }
        }
    }
}
