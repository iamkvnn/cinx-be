package com.cinx.notification.messaging.consumer;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.CourseClient;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.response.course.CourseResponse;
import com.cinx.notification.dto.response.user.UserDto;
import com.cinx.notification.messaging.event.social.CourseAnswerCreatedEvent;
import com.cinx.notification.messaging.event.social.CourseQuestionCreatedEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@RabbitListener(queues = "notification.social.queue", ackMode = "MANUAL")
public class SocialNotificationListener {

    private final CourseClient courseClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @org.springframework.amqp.rabbit.annotation.RabbitHandler
    public void handleQuestionCreated(CourseQuestionCreatedEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received question created event: {}", event.getQuestionId());
        try {
            ApiResponse<CourseResponse> courseRes = courseClient.getCourseById(event.getCourseId());
            if (courseRes != null && courseRes.success() && courseRes.data() != null) {
                CourseResponse course = courseRes.data();
                if (course.instructor() != null && course.instructor().id() != null) {
                    String instructorId = course.instructor().id();
                    
                    // Don't notify if the instructor asked the question themselves
                    if (!instructorId.equals(event.getAskedByUserId())) {
                        String title = "New Question in " + course.title();
                        String message = "A student has asked a new question: " + event.getQuestionTitle();
                        sendInAppNotification(instructorId, title, message);
                    }
                }
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing question created event", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }

    @org.springframework.amqp.rabbit.annotation.RabbitHandler
    public void handleAnswerCreated(CourseAnswerCreatedEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Received answer created event: {}", event.getAnswerId());
        try {
            String targetUserId = null;
            String message = "";
            String title = "New Reply to your Q&A";
            
            if (event.getParentAnswerAuthorId() != null) {
                targetUserId = event.getParentAnswerAuthorId();
                message = "Someone replied to your answer in a course Q&A.";
            } else if (event.getQuestionAuthorId() != null) {
                targetUserId = event.getQuestionAuthorId();
                message = "Someone answered your question in a course Q&A.";
            }

            // Don't notify if user answered their own question/answer
            if (targetUserId != null && !targetUserId.equals(event.getAnsweredByUserId())) {
                sendInAppNotification(targetUserId, title, message);
            }
            
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing answer created event", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ex) {
                log.error("Error nacking message", ex);
            }
        }
    }

    private void sendInAppNotification(String userId, String title, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userIds", List.of(userId));
        payload.put("title", title);
        payload.put("message", message);
        rabbitTemplate.convertAndSend("notification.send.exchange", "notification.in-app.send", payload);
    }
}
