package com.cinx.learning.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitVideoQuestionRequest(
        @NotBlank(message = "videoLessonId must not be blank")
        String videoLessonId,

        @NotBlank(message = "videoAssessmentId must not be blank")
        String videoAssessmentId,

        @NotBlank(message = "userAnswer must not be blank")
        String userAnswer
) {
}
