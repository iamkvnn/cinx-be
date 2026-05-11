package com.cinx.learning.messaging;

import com.cinx.learning.messaging.event.LessonChangedEvent;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonChangeEventListener {

    private final ILearningProgressService learningProgressService;

    @RabbitListener(queues = "learning.lesson-change.queue",
                    containerFactory = "rabbitListenerContainerFactory")
    public void onLessonChanged(LessonChangedEvent event) {
        log.info("Received LessonChangedEvent: courseId={} lessonId={} changeType={}",
                event.getCourseId(), event.getLessonId(), event.getChangeType());
        learningProgressService.recomputeCourseProgress(
                event.getCourseId(),
                event.getLessonId(),
                event.getChangeType()
        );
    }
}
