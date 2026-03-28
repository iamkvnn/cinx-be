package com.cinx.learning.dto.request;

import java.time.LocalDateTime;

public record CreateQuizSessionRequest(
        String quizLessonId,
        LocalDateTime startTime
) {
}
