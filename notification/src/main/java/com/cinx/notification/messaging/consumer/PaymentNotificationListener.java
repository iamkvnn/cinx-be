package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.enrollment.OrderDetailResponse;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.client.EnrollmentClient;
import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.payment.PaymentEvent;
import com.cinx.notification.service.dispatch.INotificationDispatchService;
import com.cinx.notification.service.idempotency.IdempotencyService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationListener {

    private final EnrollmentClient enrollmentClient;
    private final UserClient userClient;
    private final INotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = "notification.payment.queue", ackMode = "MANUAL")
    public void handlePaymentSuccess(PaymentEvent event, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                     @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received payment success event for order: {}", event.getOrderId());
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }
            // Enrich: payment event only carries orderId; we need userId + email
            ApiResponse<OrderDetailResponse> orderResponse = enrollmentClient.getOrderById(event.getOrderId());
            if (orderResponse == null || !orderResponse.success() || orderResponse.data() == null) {
                log.error("Failed to fetch order details for order: {}", event.getOrderId());
                channel.basicAck(tag, false);
                return;
            }

            OrderDetailResponse order = orderResponse.data();
            String userId = order.userId();

            ApiResponse<UserDto> userResponse = userClient.getUserById(userId);
            if (userResponse == null || !userResponse.success() || userResponse.data() == null) {
                log.error("Failed to fetch user details for userId: {}", userId);
                channel.basicAck(tag, false);
                return;
            }

            UserDto user = userResponse.data();
            String formattedPrice = order.totalPrice() != null
                    ? String.format("%,d VND", order.totalPrice())
                    : "Free";

            NotificationContext ctx = NotificationContext.builder()
                    .channels(List.of("EMAIL", "IN_APP"))
                    .emailPayload(Map.of(
                            "to", user.email(),
                            "subject", "Payment Confirmation - Order " + event.getOrderId(),
                            "body", String.format(
                                    "<html><body><h2>Payment Successful</h2>" +
                                    "<p>Dear %s,</p>" +
                                    "<p>We have successfully received your payment for order <b>%s</b>.</p>" +
                                    "<p>Total Amount: <b>%s</b></p>" +
                                    "<p>Thank you for your purchase!</p>" +
                                    "</body></html>",
                                    user.name(), event.getOrderId(), formattedPrice)
                    ))
                    .inAppPayload(Map.of(
                            "userIds", List.of(userId),
                            "title", "Payment Successful",
                            "message", String.format(
                                    "Your payment for order %s (%s) has been successfully processed.",
                                    event.getOrderId(), formattedPrice)
                    ))
                    .build();

            dispatchService.dispatch(ctx);
            idempotencyService.markSuccess(messageId);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Error processing payment success event: ", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }
}
