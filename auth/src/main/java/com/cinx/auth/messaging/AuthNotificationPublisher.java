package com.cinx.auth.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthNotificationPublisher {

    private static final String EXCHANGE = "auth.events.exchange";

    private final RabbitTemplate rabbitTemplate;

    /** OTP sent when a new user registers (email verification). */
    public void publishOtpVerifyEmail(String email, String otp) {
        publish("auth.otp.verify-email", Map.of(
                "to", email,
                "subject", "Mã Xác Nhận OTP",
                "body", "Mã xác nhận OTP của bạn là: " + otp
        ));
    }

    /** OTP sent for forgot-password flow. */
    public void publishOtpForgotPassword(String email, String otp) {
        publish("auth.otp.forgot-password", Map.of(
                "to", email,
                "subject", "Yêu cầu quên mật khẩu",
                "body", "Mã OTP của bạn là: " + otp
        ));
    }

    /** Notification when admin bans a user account. */
    public void publishAccountBanned(String email, String body) {
        publish("auth.account.banned", Map.of(
                "to", email,
                "subject", "Thông báo tài khoản bị khóa",
                "body", body
        ));
    }

    /** Notification when a user account is unbanned. */
    public void publishAccountUnbanned(String email) {
        publish("auth.account.unbanned", Map.of(
                "to", email,
                "subject", "Thông báo tài khoản được mở khóa",
                "body", "Tài khoản của bạn đã được mở khóa. Bạn có thể đăng nhập và sử dụng dịch vụ như bình thường."
        ));
    }

    /** Notification when a banned account is automatically unlocked on expiry. */
    public void publishAccountAutoUnbanned(String email) {
        publish("auth.account.auto-unbanned", Map.of(
                "to", email,
                "subject", "Thông báo tài khoản được tự động mở khóa",
                "body", "Tài khoản của bạn đã hết thời hạn khóa. Bạn có thể đăng nhập và sử dụng dịch vụ như bình thường."
        ));
    }

    private void publish(String routingKey, Map<String, Object> payload) {
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload, msg -> {
            msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            msg.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            return msg;
        });
        log.info("Auth event published → routingKey={}", routingKey);
    }
}
