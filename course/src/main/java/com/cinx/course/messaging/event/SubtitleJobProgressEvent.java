package com.cinx.course.messaging.event;

public record SubtitleJobProgressEvent(
        String jobId,
        Integer progressPercent
) {
}
