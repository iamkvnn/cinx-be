package com.cinx.social.messaging;

import com.cinx.social.event.CourseReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseReviewEventPublisher {
    private static final String EXCHANGE = "social.events.exchange";
    private static final String REVIEW_CREATED_ROUTING_KEY = "social.review.created";

    private final RabbitTemplate rabbitTemplate;

    public void publishReviewCreatedEvent(CourseReviewCreatedEvent event) {
        log.info("Publishing CourseReviewCreatedEvent: {}", event.getEventId());
        rabbitTemplate.convertAndSend(EXCHANGE, REVIEW_CREATED_ROUTING_KEY, event, message -> {
            message.getMessageProperties().setMessageId(
                    event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString());
            return message;
        });
    }
}
