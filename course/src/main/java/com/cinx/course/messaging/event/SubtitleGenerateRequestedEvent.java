package com.cinx.course.messaging.event;

public record SubtitleGenerateRequestedEvent(
        String jobId,
        String courseId,
        String lessonId,
        String videoFileKey,
        String targetLanguageCode,
        String displayName,
        String expectedOutputFileKey
) {
}
