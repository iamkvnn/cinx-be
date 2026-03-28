package com.cinx.learning.dto.response;

import java.time.LocalDateTime;

public record CourseProgressResponse(
    String id,
    String userId,
    String courseId,
    Boolean isCompleted,
    Boolean isPassed,
    Double avgScore,
    Integer totalItems,
    Integer completedItems,
    LocalDateTime completionTime
) {
}
