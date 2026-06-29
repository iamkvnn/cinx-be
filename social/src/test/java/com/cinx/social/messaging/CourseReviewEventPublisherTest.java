package com.cinx.social.messaging;

import com.cinx.social.event.CourseReviewCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseReviewEventPublisherTest {
    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    void enqueuesReviewCreatedToSocialExchange() {
        CourseReviewEventPublisher publisher = new CourseReviewEventPublisher(outboxEventPublisher);
        CourseReviewCreatedEvent event = CourseReviewCreatedEvent.builder()
                .eventId("event-1")
                .reviewId("review-1")
                .build();

        publisher.publishReviewCreatedEvent(event);

        verify(outboxEventPublisher).enqueue(
                eq("event-1"),
                eq("CourseReview"),
                eq("review-1"),
                eq("CourseReviewCreated"),
                eq("social.events.exchange"),
                eq("social.review.created"),
                same(event)
        );
    }
}
