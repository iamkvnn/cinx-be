package com.cinx.learning.dto.request;

public record UpdateLearningItemRequest(
        Boolean isCompleted,
        Boolean isPassed,
        Double score
) {
}
