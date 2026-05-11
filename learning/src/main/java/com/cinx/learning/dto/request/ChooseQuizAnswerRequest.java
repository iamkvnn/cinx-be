package com.cinx.learning.dto.request;
import jakarta.validation.constraints.NotBlank;


public record ChooseQuizAnswerRequest(
        @NotBlank(message = "questionId must not be blank")
        String questionId,

        @NotBlank(message = "userAnswer must not be blank")
        String userAnswer
) {
}
