package com.cinx.learning.dto.request;

public record TrackingVideoLessonRequest(
        String videoLessonId,
        Integer currentPosition
) {
}
