package com.cinx.learning.dto.response;

public record LearningItemProgressResponse(
        String itemId,
        Boolean isCompleted,
        Boolean isPassed,
        Double score
) {
}
