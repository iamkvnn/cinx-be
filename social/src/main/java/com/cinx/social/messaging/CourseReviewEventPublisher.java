package com.cinx.social.messaging;

import com.cinx.social.event.CourseReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseReviewEventPublisher {
    private static final String EXCHANGE = "social.events.exchange";
    private static final String REVIEW_CREATED_ROUTING_KEY = "social.review.created";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishReviewCreatedEvent(CourseReviewCreatedEvent event) {
        log.info("Publishing CourseReviewCreatedEvent: {}", event.getEventId());
        outboxEventPublisher.enqueue(
                event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString(),
                "CourseReview",
                event.getReviewId(),
                "CourseReviewCreated",
                EXCHANGE,
                REVIEW_CREATED_ROUTING_KEY,
                event
        );
    }
}
