package com.cinx.learning.dto.response;

public record LearningItemResultResponse(
        String itemId,
        Double score,
        Integer weight
) {
}
