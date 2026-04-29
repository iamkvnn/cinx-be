package com.cinx.course.messaging;

import com.cinx.course.messaging.event.CourseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CourseEventProducer {
    private final RabbitTemplate rabbitTemplate;

//    public void publishOrderCreatedEvent(CourseEvent event) {
//        System.out.println("Publishing course event: " + event);
//        rabbitTemplate.convertAndSend("course.events.exchange", "course.course.created", event);
//        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send",
//                Map.of("userIds", List.of("a426574e-6f71-4b3a-b7d7-145ed379b3ca"),
//                        "title", "Khóa học mới đã được tạo!",
//                        "message", "Xin chào, khóa học '" + event.getCourse().title() + "' đã được tạo thành công. Hãy kiểm tra và phê duyệt!"));
//        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.push.send",
//                Map.of("userIds", List.of("a426574e-6f71-4b3a-b7d7-145ed379b3ca"),
//                        "title", "Khóa học mới đã được tạo!",
//                        "message", "Xin chào, khóa học '" + event.getCourse().title() + "' đã được tạo thành công. Hãy kiểm tra và phê duyệt!"));
//    }
//
//    public void publishCourseUpdatedEvent(CourseEvent event) {
//        System.out.println("Publishing course updated event: " + event);
//        rabbitTemplate.convertAndSend("course.events.exchange", "course.course.updated", event);
//        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send",
//                Map.of("userIds", List.of("a426574e-6f71-4b3a-b7d7-145ed379b3ca"),
//                        "title", "Khóa học đã được cập nhật!",
//                        "message", "Xin chào, khóa học '" + event.getCourse().title() + "' đã được cập nhật. Hãy kiểm tra và phê duyệt!"));
//        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.push.send",
//                Map.of("userIds", List.of("a426574e-6f71-4b3a-b7d7-145ed379b3ca"),
//                        "title", "Khóa học đã được cập nhật!",
//                        "message", "Xin chào, khóa học '" + event.getCourse().title() + "' đã được cập nhật. Hãy kiểm tra và phê duyệt!"));
//    }
}
