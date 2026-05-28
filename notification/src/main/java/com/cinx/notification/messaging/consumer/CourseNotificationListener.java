package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.CourseClient;
import com.cinx.notification.client.EnrollmentClient;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.course.CourseResponse;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.course.LessonChangedEvent;
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
public class CourseNotificationListener {

    private final CourseClient courseClient;
    private final EnrollmentClient enrollmentClient;
    private final UserClient userClient;
    private final INotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = "notification.course.queue", ackMode = "MANUAL")
    public void handleLessonChanged(LessonChangedEvent event, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                    @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received course lesson changed event for courseId: {}", event.getCourseId());
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }
            // courseTitle may be embedded; fall back to Feign if blank
            String courseTitle = event.getCourseTitle();
            if (courseTitle == null || courseTitle.isBlank()) {
                ApiResponse<CourseResponse> courseRes = courseClient.getCourseById(event.getCourseId());
                if (courseRes == null || !courseRes.success() || courseRes.data() == null) {
                    log.warn("Course info not found for courseId: {}, skipping.", event.getCourseId());
                    channel.basicAck(tag, false);
                    return;
                }
                courseTitle = courseRes.data().title();
            }

            ApiResponse<List<String>> enrolledRes = enrollmentClient.getUserIdsEnrolledInCourse(event.getCourseId());
            if (enrolledRes == null || !enrolledRes.success()
                    || enrolledRes.data() == null || enrolledRes.data().isEmpty()) {
                log.info("No enrolled users for courseId: {}", event.getCourseId());
                channel.basicAck(tag, false);
                return;
            }

            List<String> userIds = enrolledRes.data();
            String action = switch (event.getChangeType().toUpperCase()) {
                case "CREATED" -> "added";
                case "DELETED" -> "removed";
                default -> "updated";
            };

            String title = "Course Update: " + courseTitle;
            String message = "A lesson has been " + action + " in " + courseTitle + ". Check it out!";

            // In-app: one batch for all enrolled users
            NotificationContext inAppCtx = NotificationContext.builder()
                    .channels(List.of("IN_APP"))
                    .inAppPayload(Map.of(
                            "userIds", userIds,
                            "title", title,
                            "message", message
                    ))
                    .build();
            dispatchService.dispatch(inAppCtx);

            // Email: one per user (requires their email — still needs Feign)
            for (String userId : userIds) {
                try {
                    ApiResponse<UserDto> userRes = userClient.getUserById(userId);
                    if (userRes != null && userRes.success() && userRes.data() != null) {
                        String email = userRes.data().email();
                        if (email != null && !email.isBlank()) {
                            NotificationContext emailCtx = NotificationContext.builder()
                                    .channels(List.of("EMAIL"))
                                    .emailPayload(Map.of(
                                            "to", email,
                                            "subject", title,
                                            "body", String.format(
                                                    "<html><body><h3>%s</h3><p>%s</p></body></html>",
                                                    title, message)
                                    ))
                                    .build();
                            dispatchService.dispatch(emailCtx);
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Could not send email for userId={}: {}", userId, ex.getMessage());
                }
            }

            idempotencyService.markSuccess(messageId);
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
}
