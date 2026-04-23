package com.cinx.user.messaging;

import com.cinx.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendInstructorVerifiedEmail(User user) {
        System.out.println("Publishing : " + user.getEmail());
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.email.send",
                Map.of("to", user.getEmail(),
                        "subject", "Chúc mừng bạn đã trở thành giảng viên!",
                        "body", "Xin chào " + user.getName() + ",\n\n" +
                                "Chúng tôi rất vui mừng thông báo rằng bạn đã được xác minh là giảng viên trên nền tảng của chúng tôi. " +
                                "Bạn có thể bắt đầu tạo khóa học và chia sẻ kiến thức của mình với cộng đồng học viên.\n\n" +
                                "Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.\n\n" +
                                "Chúc bạn thành công trong hành trình giảng dạy!\n\n" +
                                "Trân trọng,\n" +
                                "Đội ngũ Cinx"),
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }

    public void sendInstructorRejectedEmail(User user) {
        System.out.println("Publishing : " + user.getEmail());
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.email.send",
                Map.of("to", user.getEmail(),
                        "subject", "Thông báo về việc xác minh giảng viên",
                        "body", "Xin chào " + user.getName() + ",\n\n" +
                                "Chúng tôi rất tiếc phải thông báo rằng yêu cầu xác minh giảng viên của bạn đã bị từ chối. " +
                                "Điều này có thể do một số lý do, chẳng hạn như thông tin không đầy đủ hoặc không phù hợp với tiêu chí của chúng tôi.\n\n" +
                                "Nếu bạn muốn biết thêm chi tiết hoặc cần hỗ trợ để cải thiện hồ sơ của mình, vui lòng liên hệ với chúng tôi.\n\n" +
                                "Chúng tôi đánh giá cao sự quan tâm của bạn và hy vọng sẽ có cơ hội hợp tác trong tương lai.\n\n" +
                                "Trân trọng,\n" +
                                "Đội ngũ Cinx"),
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }

    public void sendNewInstructorNotification(User user) {
        System.out.println("Publishing : " + user.getEmail());
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send",
                Map.of("userIds", List.of("a426574e-6f71-4b3a-b7d7-145ed379b3ca"),
                        "title", "Có một giảng viên mới cần được xác minh!",
                        "message", "Xin chào, có một giảng viên mới đã đăng ký và cần được xác minh. Vui lòng kiểm tra và xác minh hồ sơ của giảng viên này để họ có thể bắt đầu tạo khóa học và chia sẻ kiến thức với cộng đồng học viên."),
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.push.send",
                Map.of("userIds", List.of("a426574e-6f71-4b3a-b7d7-145ed379b3ca"),
                        "title", "Có một giảng viên mới cần được xác minh!",
                        "message", "Xin chào, có một giảng viên mới đã đăng ký và cần được xác minh. Vui lòng kiểm tra và xác minh hồ sơ của giảng viên này để họ có thể bắt đầu tạo khóa học và chia sẻ kiến thức với cộng đồng học viên."),
                m -> { m.getMessageProperties().setMessageId(UUID.randomUUID().toString()); return m; });
    }
}
