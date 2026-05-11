package com.cinx.learning.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateCourseProgressRequest(
    @NotNull(message = "isCompleted must not be null")
    Boolean isCompleted,

    @NotNull(message = "isPassed must not be null")
    Boolean isPassed,

    @NotNull(message = "avgScore must not be null")
    Double avgScore,

    @NotNull(message = "totalItems must not be null")
    Integer totalItems,

    @NotNull(message = "completedItems must not be null")
    Integer completedItems,

    @NotNull(message = "completionTime must not be null")
    LocalDateTime completionTime
) {
}
