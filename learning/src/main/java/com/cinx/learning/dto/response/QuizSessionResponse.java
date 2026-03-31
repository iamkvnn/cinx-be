package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizSessionStatus;

import java.time.LocalDateTime;

public record QuizSessionResponse(
        String id,
        String quizLessonId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        QuizSessionStatus status,
        Boolean isReviewAllowed,
        Boolean isShowAnswersOnReview,
        QuizSessionSubmissionResponse quizSessionSubmission
) {
}
