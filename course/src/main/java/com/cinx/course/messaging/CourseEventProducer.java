package com.cinx.course.messaging;

import com.cinx.course.messaging.event.CourseRecommendationEvent;
import com.cinx.course.messaging.event.CourseApprovalRequestedEvent;
import com.cinx.course.messaging.event.CourseContentPublishedEvent;
import com.cinx.course.messaging.event.LessonChangedEvent;
import com.cinx.course.messaging.event.QuizSyncEvent;
import com.cinx.course.messaging.event.ScoringModeChangedEvent;
import com.cinx.course.messaging.event.SubtitleGenerateRequestedEvent;
import com.cinx.course.messaging.event.SubtitleTranslateRequestedEvent;
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

    public void publishCourseContentPublishedEvent(CourseContentPublishedEvent event) {
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "Course",
                event.getCourseId(),
                "CourseContentPublished",
                EXCHANGE,
                "course.content.published",
                event
        );
    }

    public void publishCourseApprovalRequestedEvent(CourseApprovalRequestedEvent event) {
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "Course",
                event.getCourseId(),
                "CourseApprovalRequested",
                EXCHANGE,
                "course.approval.requested",
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

    public void publishCourseRecommendationEvent(String routingKey, String eventType, CourseRecommendationEvent event) {
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "Course",
                event.course().id(),
                eventType,
                EXCHANGE,
                routingKey,
                event
        );
    }

    public void publishSubtitleGenerateRequestedEvent(SubtitleGenerateRequestedEvent event) {
        outboxEventPublisher.enqueue(
                event.jobId(),
                "SubtitleJob",
                event.jobId(),
                "SubtitleGenerateRequested",
                EXCHANGE,
                "course.subtitle.generate.requested",
                event
        );
    }

    public void publishSubtitleTranslateRequestedEvent(SubtitleTranslateRequestedEvent event) {
        outboxEventPublisher.enqueue(
                event.jobId(),
                "SubtitleJob",
                event.jobId(),
                "SubtitleTranslateRequested",
                EXCHANGE,
                "course.subtitle.translate.requested",
                event
        );
    }
}
