package com.cinx.learning.dto.response;

public record VideoLessonTrackingHistoryResponse(
        String userId,
        String videoLessonId,
        Integer currentPosition,
        String lastTrackingTime
) {
}
