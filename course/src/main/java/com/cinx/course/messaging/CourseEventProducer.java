package com.cinx.course.messaging;

import com.cinx.course.messaging.event.LessonChangedEvent;
import com.cinx.course.messaging.event.QuizSyncEvent;
import com.cinx.course.messaging.event.ScoringModeChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourseEventProducer {
    private static final String EXCHANGE = "course.events.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishLessonChangedEvent(LessonChangedEvent event) {
        System.out.println("Publishing lesson updated event: " + event);
        outboxEventPublisher.enqueue(
                event.getCourseId() + "-" + event.getLessonId() + "-" + event.getChangeType(),
                "Lesson",
                event.getLessonId(),
                "LessonChanged",
                EXCHANGE,
                "course.lesson.changed",
                event
        );
    }

    public void publishQuizSyncEvent(QuizSyncEvent event) {
        System.out.println("Publishing quiz sync event: quizLessonId=" + event.getQuizLessonId());
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "QuizLesson",
                event.getQuizLessonId(),
                "QuizSyncAndRegrade",
                EXCHANGE,
                "course.quiz.sync-and-regrade",
                event
        );
    }

    public void publishScoringModeChangedEvent(ScoringModeChangedEvent event) {
        System.out.println("Publishing quiz sync event: quizLessonId=" + event.quizLessonId());
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "QuizLesson",
                event.quizLessonId(),
                "QuizScoringModeChanged",
                EXCHANGE,
                "course.quiz.scoring-mode-changed",
                event
        );
    }
}
