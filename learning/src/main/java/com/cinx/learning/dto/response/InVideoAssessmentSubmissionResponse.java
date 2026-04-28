package com.cinx.learning.dto.response;

import java.time.LocalDateTime;

public record InVideoAssessmentSubmissionResponse(
        String videoLessonId,
        String videoAssessmentId,
        String userAnswer,
        LocalDateTime submissionTime
) {
}
