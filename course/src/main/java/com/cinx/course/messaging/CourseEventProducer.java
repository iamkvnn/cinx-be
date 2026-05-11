package com.cinx.course.messaging;

import com.cinx.course.messaging.event.LessonChangedEvent;
import com.cinx.course.messaging.event.QuizSyncEvent;
import com.cinx.course.messaging.event.ScoringModeChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishLessonChangedEvent(LessonChangedEvent event) {
        System.out.println("Publishing lesson updated event: " + event);
        rabbitTemplate.convertAndSend("course.events.exchange", "course.lesson.changed", event);
    }

    public void publishQuizSyncEvent(QuizSyncEvent event) {
        System.out.println("Publishing quiz sync event: quizLessonId=" + event.getQuizLessonId());
        rabbitTemplate.convertAndSend("course.events.exchange", "course.quiz.sync_and_regrade", event);
    }

    public void publishScoringModeChangedEvent(ScoringModeChangedEvent event) {
        System.out.println("Publishing quiz sync event: quizLessonId=" + event.quizLessonId());
        rabbitTemplate.convertAndSend("course.events.exchange", "course.quiz.scoring_mode_changed", event);
    }
}
