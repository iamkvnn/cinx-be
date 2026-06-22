package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.EnrollmentClient;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.course.CourseContentPublishedEvent;
import com.cinx.notification.service.format.NotificationFormatter;
import com.cinx.notification.service.format.NotificationMessage;
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

    private final EnrollmentClient enrollmentClient;
    private final UserClient userClient;
    private final INotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;
    private final NotificationFormatter notificationFormatter;

    @RabbitListener(queues = "notification.course.queue", ackMode = "MANUAL")
    public void handleCourseContentPublished(CourseContentPublishedEvent event, Channel channel,
                                             @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                             @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received course content published event for courseId: {}", event.getCourseId());
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }
            String courseTitle = event.getCourseTitle();
            if (courseTitle == null || courseTitle.isBlank()) {
                courseTitle = "your course";
            }

            notifyInstructorCoursePublished(event, courseTitle);

            ApiResponse<List<String>> enrolledRes = enrollmentClient.getUserIdsEnrolledInCourse(event.getCourseId());
            if (enrolledRes == null || !enrolledRes.success()
                    || enrolledRes.data() == null || enrolledRes.data().isEmpty()) {
                log.info("No enrolled users for courseId: {}", event.getCourseId());
                channel.basicAck(tag, false);
                return;
            }

            List<String> userIds = enrolledRes.data();

            NotificationMessage notification = notificationFormatter.coursePublishedForLearners(
                    event.getCourseId(), courseTitle);

            // In-app: one batch for all enrolled users
            NotificationContext inAppCtx = NotificationContext.builder()
                    .channels(List.of("IN_APP"))
                    .inAppPayload(notification.inAppPayload(userIds))
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
                                    .emailPayload(notification.emailPayload(email))
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
            log.error("Error processing course content published event: ", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }

    private void notifyInstructorCoursePublished(CourseContentPublishedEvent event, String courseTitle) {
        if (event.getInstructorId() == null || event.getInstructorId().isBlank()) {
            return;
        }

        ApiResponse<UserDto> userRes = userClient.getUserById(event.getInstructorId());
        if (userRes == null || !userRes.success() || userRes.data() == null) {
            throw new IllegalStateException("Failed to fetch instructor details for userId: " + event.getInstructorId());
        }

        UserDto instructor = userRes.data();
        if (instructor.email() == null || instructor.email().isBlank()) {
            throw new IllegalStateException("Instructor email is missing for userId: " + event.getInstructorId());
        }
        NotificationMessage notification = notificationFormatter.coursePublishedForInstructor(
                event.getCourseId(), courseTitle, instructor.name());

        NotificationContext ctx = NotificationContext.builder()
                .channels(List.of("EMAIL", "IN_APP"))
                .emailPayload(notification.emailPayload(instructor.email()))
                .inAppPayload(notification.inAppPayload(List.of(event.getInstructorId())))
                .build();
        dispatchService.dispatch(ctx);
    }
}
