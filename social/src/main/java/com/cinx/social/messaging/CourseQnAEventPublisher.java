package com.cinx.social.messaging;

import com.cinx.social.event.CourseAnswerCreatedEvent;
import com.cinx.social.event.CourseQuestionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseQnAEventPublisher {
    private static final String EXCHANGE = "course.qna.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishQuestionCreatedEvent(CourseQuestionCreatedEvent event) {
        log.info("Publishing CourseQuestionCreatedEvent: {}", event.getEventId());
        outboxEventPublisher.enqueue(
                event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString(),
                "CourseQuestion",
                event.getQuestionId(),
                "CourseQuestionCreated",
                EXCHANGE,
                "question.created",
                event
        );
    }

    public void publishAnswerCreatedEvent(CourseAnswerCreatedEvent event) {
        log.info("Publishing CourseAnswerCreatedEvent: {}", event.getEventId());
        outboxEventPublisher.enqueue(
                event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString(),
                "CourseAnswer",
                event.getAnswerId(),
                "CourseAnswerCreated",
                EXCHANGE,
                "answer.created",
                event
        );
    }
}
