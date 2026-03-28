package com.cinx.course.messaging;

import com.cinx.course.messaging.event.CourseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreatedEvent(CourseEvent event) {
        System.out.println("Publishing course event: " + event);
        rabbitTemplate.convertAndSend("course.events.exchange", "course.course.created", event);
    }

    public void publishCourseUpdatedEvent(CourseEvent event) {
        System.out.println("Publishing course updated event: " + event);
        rabbitTemplate.convertAndSend("course.events.exchange", "course.course.updated", event);
    }
}
