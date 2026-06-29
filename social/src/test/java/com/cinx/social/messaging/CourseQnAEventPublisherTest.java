package com.cinx.social.messaging;

import com.cinx.social.event.CourseAnswerCreatedEvent;
import com.cinx.social.event.CourseQuestionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseQnAEventPublisherTest {
    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    void enqueuesQuestionCreatedToCourseQnaExchange() {
        CourseQnAEventPublisher publisher = new CourseQnAEventPublisher(outboxEventPublisher);
        CourseQuestionCreatedEvent event = CourseQuestionCreatedEvent.builder()
                .eventId("event-1")
                .questionId("question-1")
                .build();

        publisher.publishQuestionCreatedEvent(event);

        verify(outboxEventPublisher).enqueue(
                eq("event-1"),
                eq("CourseQuestion"),
                eq("question-1"),
                eq("CourseQuestionCreated"),
                eq("course.qna.exchange"),
                eq("question.created"),
                same(event)
        );
    }

    @Test
    void enqueuesAnswerCreatedToCourseQnaExchange() {
        CourseQnAEventPublisher publisher = new CourseQnAEventPublisher(outboxEventPublisher);
        CourseAnswerCreatedEvent event = CourseAnswerCreatedEvent.builder()
                .eventId("event-2")
                .answerId("answer-1")
                .build();

        publisher.publishAnswerCreatedEvent(event);

        verify(outboxEventPublisher).enqueue(
                eq("event-2"),
                eq("CourseAnswer"),
                eq("answer-1"),
                eq("CourseAnswerCreated"),
                eq("course.qna.exchange"),
                eq("answer.created"),
                same(event)
        );
    }
}
