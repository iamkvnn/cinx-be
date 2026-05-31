package com.cinx.enrollment.messaging;

import com.cinx.enrollment.messaging.event.EnrolledCourseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrolledCourseEventProducer {
    private static final String EXCHANGE = "enrollment.events.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishEnrolledCourseCreatedEvent(EnrolledCourseEvent event) {
        System.out.println("Publishing EnrolledCourseEvent: " + event);
        outboxEventPublisher.enqueue(
                event.getUserId() + "-" + event.getCourseId() + "-ENROLLED",
                "EnrolledCourse",
                event.getUserId() + ":" + event.getCourseId(),
                "EnrollmentCreated",
                EXCHANGE,
                "enrollment.enrollment.created",
                event
        );
    }
}
