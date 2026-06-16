package com.cinx.learning.messaging;

import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.messaging.event.CertificateApprovedEvent;
import com.cinx.learning.messaging.event.CertificateRequestedEvent;
import com.cinx.learning.messaging.event.CourseCompletedEvent;
import com.cinx.learning.messaging.event.DailyReminderDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private static final String EXCHANGE = "learning.events.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishCourseCompleted(String userId, String courseId, String courseTitle) {
        CourseCompletedEvent event = CourseCompletedEvent.builder()
                .userId(userId)
                .courseId(courseId)
                .courseTitle(courseTitle)
                .occurredAt(Instant.now())
                .build();
        publish("learning.course.completed", event);
        log.info("CourseCompletedEvent published - userId={}, courseId={}", userId, courseId);
    }

    public void publishDailyReminderDue(
            String userId,
            DailyGoalType goalType,
            int targetValue,
            int currentValue,
            String targetItemId) {
        DailyReminderDueEvent event = DailyReminderDueEvent.builder()
                .userId(userId)
                .goalType(goalType.name())
                .targetValue(targetValue)
                .currentValue(currentValue)
                .targetItemId(targetItemId)
                .occurredAt(Instant.now())
                .build();
        publish("learning.reminder.due", event);
        log.info("DailyReminderDueEvent published - userId={}, goalType={}", userId, goalType);
    }

    public void publishCertificateRequested(CertificateRequestedEvent event) {
        publish("learning.certificate.requested", event);
        log.info("CertificateRequestedEvent published - requestId={}, instructorId={}",
                event.getRequestId(), event.getInstructorId());
    }

    public void publishCertificateApproved(CertificateApprovedEvent event) {
        publish("learning.certificate.approved", event);
        log.info("CertificateApprovedEvent published - requestId={}, userId={}",
                event.getRequestId(), event.getUserId());
    }

    private void publish(String routingKey, Object event) {
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "LearningNotification",
                routingKey,
                routingKey,
                EXCHANGE,
                routingKey,
                event
        );
    }
}
