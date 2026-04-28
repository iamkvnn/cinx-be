package com.cinx.learning.dto.request;

public record SubmitVideoQuestionRequest(
        String videoLessonId,
        String videoAssessmentId,
        String userAnswer
) {
}
