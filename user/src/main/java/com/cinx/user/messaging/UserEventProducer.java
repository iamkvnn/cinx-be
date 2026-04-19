package com.cinx.user.messaging;

import com.cinx.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendInstructorVerifiedEmail(User user) {
        System.out.println("Publishing : " + user.getEmail());
        rabbitTemplate.convertAndSend("user.events.exchange", "user.email.send",
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
        rabbitTemplate.convertAndSend("user.events.exchange", "user.email.send",
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
}
