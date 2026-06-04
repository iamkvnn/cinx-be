package com.cinx.user.messaging;

import com.cinx.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private static final String EXCHANGE = "user.events.exchange";

    private final RabbitTemplate rabbitTemplate;

    /** Email sent when an instructor application is approved. */
    public void sendInstructorVerifiedEmail(User user) {
        publish("user.instructor.verified", Map.of(
                "to", user.getEmail(),
                "subject", "Chúc mừng bạn đã trở thành giảng viên!",
                "body", "Xin chào " + user.getName() + ",\n\n" +
                        "Chúng tôi rất vui mừng thông báo rằng bạn đã được xác minh là giảng viên trên nền tảng của chúng tôi. " +
                        "Bạn có thể bắt đầu tạo khóa học và chia sẻ kiến thức của mình với cộng đồng học viên.\n\n" +
                        "Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.\n\n" +
                        "Chúc bạn thành công trong hành trình giảng dạy!\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ Cinx"
        ));
    }

    /** Email sent when an instructor application is rejected. */
    public void sendInstructorRejectedEmail(User user) {
        publish("user.instructor.rejected", Map.of(
                "to", user.getEmail(),
                "subject", "Thông báo về việc xác minh giảng viên",
                "body", "Xin chào " + user.getName() + ",\n\n" +
                        "Chúng tôi rất tiếc phải thông báo rằng yêu cầu xác minh giảng viên của bạn đã bị từ chối. " +
                        "Điều này có thể do một số lý do, chẳng hạn như thông tin không đầy đủ hoặc không phù hợp với tiêu chí của chúng tôi.\n\n" +
                        "Nếu bạn muốn biết thêm chi tiết hoặc cần hỗ trợ để cải thiện hồ sơ của mình, vui lòng liên hệ với chúng tôi.\n\n" +
                        "Chúng tôi đánh giá cao sự quan tâm của bạn và hy vọng sẽ có cơ hội hợp tác trong tương lai.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ Cinx"
        ));
    }

    /**
     * In-app + push notification to admin when a new instructor registers.
     * The admin userId is externalized here — consider moving it to application.yml if it changes.
     */
    public void sendNewInstructorNotification(User user) {
        String adminUserId = "a426574e-6f71-4b3a-b7d7-145ed379b3ca";
        String title = "Có một giảng viên mới cần được xác minh!";
        String message = "Xin chào, có một giảng viên mới đã đăng ký và cần được xác minh. " +
                "Vui lòng kiểm tra và xác minh hồ sơ của giảng viên này để họ có thể bắt đầu tạo khóa học.";

        publish("user.instructor.pending", Map.of(
                "userIds", List.of(adminUserId),
                "title", title,
                "message", message,
                "instructorEmail", user.getEmail()
        ));
    }

    public void publishPreferredCategoriesUpdated(String userId, List<String> categoryIds) {
        publish("user.preference.updated", Map.of(
                "payload", Map.of(
                        "userId", userId,
                        "categoryIds", categoryIds
                )
        ));
    }

    private void publish(String routingKey, Map<String, Object> payload) {
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, payload, msg -> {
            msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            msg.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            return msg;
        });
        log.info("User event published → routingKey={}", routingKey);
    }
}
