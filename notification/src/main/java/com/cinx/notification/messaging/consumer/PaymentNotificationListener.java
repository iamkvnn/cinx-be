package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.EnrollmentClient;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.enrollment.OrderDetailResponse;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.event.payment.PaymentEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationListener {

    private final EnrollmentClient enrollmentClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "notification.payment.queue", ackMode = "MANUAL")
    public void handlePaymentSuccess(PaymentEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received payment success event for order: {}", event.getOrderId());
        
        try {
            // Fetch order details
            ApiResponse<OrderDetailResponse> orderResponse = enrollmentClient.getOrderById(event.getOrderId());
            if (orderResponse == null || !orderResponse.success() || orderResponse.data() == null) {
                log.error("Failed to fetch order details for order: {}", event.getOrderId());
                channel.basicAck(tag, false);
                return;
            }
            
            OrderDetailResponse order = orderResponse.data();
            String userId = order.userId();
            
            // Fetch user info
            ApiResponse<UserDto> userResponse = userClient.getUserById(userId);
            if (userResponse == null || !userResponse.success() || userResponse.data() == null) {
                log.error("Failed to fetch user details for user: {}", userId);
                channel.basicAck(tag, false);
                return;
            }
            
            UserDto user = userResponse.data();

            // Format total price (simplistic formatting, could be improved)
            String formattedPrice = order.totalPrice() != null ? String.format("%,d VND", order.totalPrice()) : "Free";

            // 1. Send Email Notification
            sendEmailNotification(user.email(), user.name(), event.getOrderId(), formattedPrice, event.getOrderId() + "-email");

            // 2. Send In-App Notification
            sendInAppNotification(userId, event.getOrderId(), formattedPrice, event.getOrderId() + "-inapp");

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing payment success event: ", e);
            try {
                // Reject and requeue or DLQ
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }

    private void sendEmailNotification(String email, String name, String orderId, String price, String idempotencyKey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", email);
        payload.put("subject", "Payment Confirmation - Order " + orderId);
        payload.put("body", String.format("<html><body>" +
                "<h2>Payment Successful</h2>" +
                "<p>Dear %s,</p>" +
                "<p>We have successfully received your payment for order <b>%s</b>.</p>" +
                "<p>Total Amount: <b>%s</b></p>" +
                "<p>Thank you for your purchase!</p>" +
                "</body></html>", name, orderId, price));
        payload.put("idempotencyKey", idempotencyKey);

        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.email.send", payload);
    }

    private void sendInAppNotification(String userId, String orderId, String price, String idempotencyKey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userIds", List.of(userId));
        payload.put("title", "Payment Successful");
        payload.put("message", String.format("Your payment for order %s (%s) has been successfully processed.", orderId, price));
        payload.put("idempotencyKey", idempotencyKey);

        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send", payload);
    }
}
