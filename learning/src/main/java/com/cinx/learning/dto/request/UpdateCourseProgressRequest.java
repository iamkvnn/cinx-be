package com.cinx.learning.dto.request;

import java.time.LocalDateTime;

public record UpdateCourseProgressRequest(
        Boolean isCompleted,
        Boolean isPassed,
        Double avgScore,
        Integer totalItems,
        Integer completedItems,
        LocalDateTime completionTime
) {
}
