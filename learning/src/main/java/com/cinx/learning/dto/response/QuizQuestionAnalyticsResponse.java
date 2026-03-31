package com.cinx.learning.dto.response;

public record QuizQuestionAnalyticsResponse(
    String questionId,
    Integer totalAttempts,
    Integer correctAttempts,
    Double accuracy
) {}