package com.cinx.course.messaging.event;

public record SubtitleJobFailedEvent(
        String jobId,
        String errorCode,
        String errorMessage
) {
}
