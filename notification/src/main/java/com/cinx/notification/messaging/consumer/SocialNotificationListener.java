package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.CourseClient;
import com.cinx.notification.dto.response.course.CourseResponse;
import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.social.CourseAnswerCreatedEvent;
import com.cinx.notification.messaging.event.social.CourseQuestionCreatedEvent;
import com.cinx.notification.service.dispatch.INotificationDispatchService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "notification.social.queue", ackMode = "MANUAL")
public class SocialNotificationListener {

    private final CourseClient courseClient;
    private final INotificationDispatchService dispatchService;

    @RabbitHandler
    public void handleQuestionCreated(CourseQuestionCreatedEvent event, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received question created event: {}", event.getQuestionId());
        try {
            // Enrich: need instructorId, not available in event
            ApiResponse<CourseResponse> courseRes = courseClient.getCourseById(event.getCourseId());
            if (courseRes != null && courseRes.success() && courseRes.data() != null) {
                CourseResponse course = courseRes.data();
                if (course.instructor() != null && course.instructor().id() != null) {
                    String instructorId = course.instructor().id();

                    if (!instructorId.equals(event.getAskedByUserId())) {
                        NotificationContext ctx = NotificationContext.builder()
                                .channels(List.of("IN_APP"))
                                .inAppPayload(Map.of(
                                        "userIds", List.of(instructorId),
                                        "title", "New Question in " + course.title(),
                                        "message", "A student has asked a new question: " + event.getQuestionTitle()
                                ))
                                .build();
                        dispatchService.dispatch(ctx);
                    }
                }
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing question created event", e);
            nack(channel, tag);
        }
    }

    @RabbitHandler
    public void handleAnswerCreated(CourseAnswerCreatedEvent event, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received answer created event: {}", event.getAnswerId());
        try {
            String targetUserId = null;
            String message;

            if (event.getParentAnswerAuthorId() != null) {
                targetUserId = event.getParentAnswerAuthorId();
                message = "Someone replied to your answer in a course Q&A.";
            } else if (event.getQuestionAuthorId() != null) {
                targetUserId = event.getQuestionAuthorId();
                message = "Someone answered your question in a course Q&A.";
            } else {
                channel.basicAck(tag, false);
                return;
            }

            if (!targetUserId.equals(event.getAnsweredByUserId())) {
                NotificationContext ctx = NotificationContext.builder()
                        .channels(List.of("IN_APP"))
                        .inAppPayload(Map.of(
                                "userIds", List.of(targetUserId),
                                "title", "New Reply to your Q&A",
                                "message", message
                        ))
                        .build();
                dispatchService.dispatch(ctx);
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing answer created event", e);
            nack(channel, tag);
        }
    }

    private void nack(Channel channel, long tag) {
        try {
            channel.basicNack(tag, false, false);
        } catch (Exception ex) {
            log.error("Error nacking message", ex);
        }
    }
}
