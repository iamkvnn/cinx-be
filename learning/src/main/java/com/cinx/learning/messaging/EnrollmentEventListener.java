package com.cinx.learning.messaging;

import com.cinx.learning.messaging.event.EnrolledCourseEvent;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.learningPath.ILearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {
    private final ILearningProgressService learningProgressService;
    private final ILearningPathService learningPathService;

    @RabbitListener(queues = "learning.enrollment.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveEnrolledCourseMessage(EnrolledCourseEvent event) {
        System.out.println("Received event message: " + event);
        learningProgressService.createCourseProgress(event.getUserId(), event.getCourseId());
        learningPathService.activatePendingPathForCourse(event.getUserId(), event.getCourseId());
    }
}
