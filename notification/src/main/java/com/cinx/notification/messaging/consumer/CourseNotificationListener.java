package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.CourseClient;
import com.cinx.notification.client.EnrollmentClient;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.course.CourseResponse;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.event.course.LessonChangedEvent;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseNotificationListener {

    private final CourseClient courseClient;
    private final EnrollmentClient enrollmentClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "notification.course.queue", ackMode = "MANUAL")
    public void handleLessonChanged(LessonChangedEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received course lesson changed event for courseId: {}", event.getCourseId());

        try {
            // Get course info
            ApiResponse<CourseResponse> courseRes = courseClient.getCourseById(event.getCourseId());
            if (courseRes == null || !courseRes.success() || courseRes.data() == null) {
                log.warn("Course info not found, skipping course notification");
                channel.basicAck(tag, false);
                return;
            }
            CourseResponse course = courseRes.data();

            // Get enrolled users
            ApiResponse<List<String>> enrolledRes = enrollmentClient.getUserIdsEnrolledInCourse(event.getCourseId());
            if (enrolledRes == null || !enrolledRes.success() || enrolledRes.data() == null || enrolledRes.data().isEmpty()) {
                log.info("No enrolled users for courseId: {}, no one to notify", event.getCourseId());
                channel.basicAck(tag, false);
                return;
            }
            List<String> userIds = enrolledRes.data();

            String action = "updated";
            if ("CREATE".equalsIgnoreCase(event.getChangeType())) {
                action = "added";
            } else if ("DELETE".equalsIgnoreCase(event.getChangeType())) {
                action = "removed";
            }

            String title = "Course Update: " + course.title();
            String message = "A lesson has been " + action + " in " + course.title() + ". Check it out!";

            // Process users (send In-App as a batch usually, Email individually)
            Map<String, Object> inAppPayload = new HashMap<>();
            inAppPayload.put("userIds", userIds);
            inAppPayload.put("title", title);
            inAppPayload.put("message", message);
            rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send", inAppPayload);

            // Send emails
            for (String userId : userIds) {
                ApiResponse<UserDto> userRes = userClient.getUserById(userId);
                if (userRes != null && userRes.success() && userRes.data() != null) {
                    UserDto user = userRes.data();
                    if (user.email() != null && !user.email().isBlank()) {
                        sendEmailNotification(user.email(), title, message);
                    }
                }
            }

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing course lesson changed event: ", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }

    private void sendEmailNotification(String email, String subject, String body) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", email);
        payload.put("subject", subject);
        payload.put("body", String.format("<html><body><h3>%s</h3><p>%s</p></body></html>", subject, body));
        
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.email.send", payload);
    }
}
