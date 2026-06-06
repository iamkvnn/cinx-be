package com.cinx.learning.messaging;

import com.cinx.learning.messaging.event.EnrolledCourseEvent;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.learningPath.ILearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {
    private final ILearningProgressService learningProgressService;
    private final ILearningPathService learningPathService;

    @RabbitListener(queues = "learning.enrollment.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receiveEnrolledCourseMessage(EnrolledCourseEvent event) {
        log.info("Received EnrolledCourseEvent: userId={}, courseId={}", event.getUserId(), event.getCourseId());
        learningProgressService.createCourseProgress(event.getUserId(), event.getCourseId());
        learningPathService.activatePendingPathForCourse(event.getUserId(), event.getCourseId());
    }
}
