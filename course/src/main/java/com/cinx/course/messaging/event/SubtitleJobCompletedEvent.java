package com.cinx.course.messaging.event;

public record SubtitleJobCompletedEvent(
        String jobId,
        String outputFileKey,
        String outputFileUrl,
        String fileName,
        String fileType,
        Long fileSize,
        String languageCode,
        String displayName,
        String wordConfidenceFileKey,
        String wordConfidenceFileUrl
) {
}
