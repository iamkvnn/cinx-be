package com.cinx.social.messaging;

import com.cinx.social.event.CourseAnswerCreatedEvent;
import com.cinx.social.event.CourseQuestionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseQnAEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishQuestionCreatedEvent(CourseQuestionCreatedEvent event) {
        log.info("Publishing CourseQuestionCreatedEvent: {}", event.getEventId());
        rabbitTemplate.convertAndSend("course.qna.exchange", "question.created", event, message -> {
            message.getMessageProperties().setMessageId(event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString());
            return message;
        });
    }

    public void publishAnswerCreatedEvent(CourseAnswerCreatedEvent event) {
        log.info("Publishing CourseAnswerCreatedEvent: {}", event.getEventId());
        rabbitTemplate.convertAndSend("course.qna.exchange", "answer.created", event, message -> {
            message.getMessageProperties().setMessageId(event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString());
            return message;
        });
    }
}
