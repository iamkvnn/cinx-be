package com.cinx.notification.messaging.consumer;

import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.learning.CourseCompletedEvent;
import com.cinx.notification.messaging.event.learning.DailyReminderDueEvent;
import com.cinx.notification.service.dispatch.INotificationDispatchService;
import com.cinx.notification.service.idempotency.IdempotencyService;
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
@RabbitListener(queues = "notification.learning.queue", ackMode = "MANUAL")
public class LearningNotificationListener {

    private final INotificationDispatchService dispatchService;
    private final IdempotencyService idempotencyService;

    @RabbitHandler
    public void handleCourseCompleted(CourseCompletedEvent event, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                      @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received course completed event for userId={}, courseId={}", event.getUserId(), event.getCourseId());
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }
            String title = "Course Completed!";
            String message = "Congratulations on completing: " + event.getCourseTitle();

            NotificationContext ctx = NotificationContext.builder()
                    .channels(List.of("IN_APP", "PUSH"))
                    .inAppPayload(Map.of(
                            "userIds", List.of(event.getUserId()),
                            "title", title,
                            "message", message
                    ))
                    .pushPayload(Map.of(
                            "userIds", List.of(event.getUserId()),
                            "title", title,
                            "body", message,
                            "data", Map.of("courseId", event.getCourseId())
                    ))
                    .build();

            dispatchService.dispatch(ctx);
            idempotencyService.markSuccess(messageId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing course completed event", e);
            nack(channel, tag);
        }
    }

    @RabbitHandler
    public void handleDailyReminderDue(DailyReminderDueEvent event, Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                       @Header(value = AmqpHeaders.MESSAGE_ID, required = false) String messageId) {
        log.info("Received daily reminder event for userId={}", event.getUserId());
        try {
            if (idempotencyService.isProcessed(messageId)) {
                channel.basicAck(tag, false);
                return;
            }
            String title = "Don't break your momentum!";
            String message = buildDailyGoalReminderMessage(event);

            NotificationContext ctx = NotificationContext.builder()
                    .channels(List.of("IN_APP", "PUSH"))
                    .inAppPayload(Map.of(
                            "userIds", List.of(event.getUserId()),
                            "title", title,
                            "message", message
                    ))
                    .pushPayload(Map.of(
                            "userIds", List.of(event.getUserId()),
                            "title", title,
                            "body", message,
                            "data", buildDailyGoalPushData(event)
                    ))
                    .build();

            dispatchService.dispatch(ctx);
            idempotencyService.markSuccess(messageId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing daily reminder event", e);
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

    private String buildDailyGoalReminderMessage(DailyReminderDueEvent event) {
        String goalType = event.getGoalType() != null ? event.getGoalType() : "";
        return switch (goalType) {
            case "XP" -> "You have a goal of " + event.getTargetValue() + " XP today. Keep learning!";
            case "LEARNING_ITEMS_COMPLETED" -> "You have a goal of completing " + event.getTargetValue() + " learning item(s) today.";
            case "VIDEOS_COMPLETED" -> "You have a goal of completing " + event.getTargetValue() + " video lesson(s) today.";
            case "QUIZZES_PASSED" -> "You have a goal of passing " + event.getTargetValue() + " quiz(zes) today.";
            case "ASSIGNMENTS_SUBMITTED" -> "You have a goal of submitting " + event.getTargetValue() + " assignment(s) today.";
            case "SPECIFIC_LESSON_COMPLETED" -> "You have a specific lesson to complete today. Keep learning!";
            default -> "You have a learning goal to complete today. Keep learning!";
        };
    }

    private Map<String, String> buildDailyGoalPushData(DailyReminderDueEvent event) {
        String goalType = event.getGoalType() != null ? event.getGoalType() : "";
        if (event.getTargetItemId() == null) {
            return Map.of(
                    "goalType", goalType,
                    "targetValue", String.valueOf(event.getTargetValue()),
                    "currentValue", String.valueOf(event.getCurrentValue())
            );
        }
        return Map.of(
                "goalType", goalType,
                "targetValue", String.valueOf(event.getTargetValue()),
                "currentValue", String.valueOf(event.getCurrentValue()),
                "targetItemId", event.getTargetItemId()
        );
    }
}
