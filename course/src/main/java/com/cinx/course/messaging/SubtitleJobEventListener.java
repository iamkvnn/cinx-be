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
        byte[] body = message.getBody();
        switch (routingKey) {
            case "ai.subtitle.job.progress" -> {
                SubtitleJobProgressEvent event = objectMapper.readValue(body, SubtitleJobProgressEvent.class);
                subtitleJobService.markProcessing(event.jobId(), event.progressPercent());
            }
            case "ai.subtitle.job.completed" -> {
                SubtitleJobCompletedEvent event = objectMapper.readValue(body, SubtitleJobCompletedEvent.class);
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
                subtitleJobService.markFailed(event.jobId(), event.errorCode(), event.errorMessage());
            }
            default -> log.warn("Ignored unsupported subtitle AI routing key: {}", routingKey);
        }
    }
}
