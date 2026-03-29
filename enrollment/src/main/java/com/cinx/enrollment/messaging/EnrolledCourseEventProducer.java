package com.cinx.enrollment.messaging;

import com.cinx.enrollment.messaging.event.EnrolledCourseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrolledCourseEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishEnrolledCourseCreatedEvent(EnrolledCourseEvent event) {
        System.out.println("Publishing EnrolledCourseEvent: " + event);
        rabbitTemplate.convertAndSend("enrollment.events.exchange", "enrollment.enrollment.created", event);
    }
}
