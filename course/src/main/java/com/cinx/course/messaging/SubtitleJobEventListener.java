package com.cinx.course.messaging;

import com.cinx.course.dto.request.SubtitleJobCompletedRequest;
import com.cinx.course.messaging.event.SubtitleJobCompletedEvent;
import com.cinx.course.messaging.event.SubtitleJobFailedEvent;
import com.cinx.course.messaging.event.SubtitleJobProgressEvent;
import com.cinx.course.service.subtitle.ISubtitleJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubtitleJobEventListener {
    private final ObjectMapper objectMapper;
    private final ISubtitleJobService subtitleJobService;

    @RabbitListener(queues = "course.subtitle.ai.queue")
    public void onSubtitleJobEvent(Message message) throws IOException {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String messageId = message.getMessageProperties().getMessageId();
        byte[] body = message.getBody();
        try {
            switch (routingKey) {
                case "ai.subtitle.job.progress" -> {
                    SubtitleJobProgressEvent event = objectMapper.readValue(body, SubtitleJobProgressEvent.class);
                    log.info("Received subtitle AI progress event jobId={} progress={}", event.jobId(), event.progressPercent());
                    subtitleJobService.markProcessing(event.jobId(), event.progressPercent());
                }
                case "ai.subtitle.job.completed" -> {
                    SubtitleJobCompletedEvent event = objectMapper.readValue(body, SubtitleJobCompletedEvent.class);
                    log.info("Received subtitle AI completed event jobId={} outputFileKey={}", event.jobId(), event.outputFileKey());
                    subtitleJobService.markCompleted(new SubtitleJobCompletedRequest(
                            event.jobId(),
                            event.outputFileKey(),
                            event.outputFileUrl(),
                            event.fileName(),
                            event.fileType(),
                            event.fileSize(),
                            event.languageCode(),
                            event.displayName(),
                            event.wordConfidenceFileKey(),
                            event.wordConfidenceFileUrl()
                    ));
                }
                case "ai.subtitle.job.failed" -> {
                    SubtitleJobFailedEvent event = objectMapper.readValue(body, SubtitleJobFailedEvent.class);
                    log.info("Received subtitle AI failed event jobId={} errorCode={}", event.jobId(), event.errorCode());
                    subtitleJobService.markFailed(event.jobId(), event.errorCode(), event.errorMessage());
                }
                default -> log.warn("Ignored unsupported subtitle AI routing key: {}", routingKey);
            }
        } catch (Exception ex) {
            log.error(
                    "Failed to handle subtitle AI event routingKey={} messageId={} body={}",
                    routingKey,
                    messageId,
                    new String(body),
                    ex
            );
            throw ex;
        }
    }
}
