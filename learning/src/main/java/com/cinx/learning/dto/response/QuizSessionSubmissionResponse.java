package com.cinx.learning.dto.response;

import java.time.LocalDateTime;

public record QuizSessionSubmissionResponse(
        String id,
        String userId,
        LocalDateTime submissionTime,
        String quizSessionId,
        Integer totalCorrectAnswers,
        Double score
) {
}
