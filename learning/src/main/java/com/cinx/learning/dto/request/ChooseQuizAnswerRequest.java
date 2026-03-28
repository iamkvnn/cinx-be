package com.cinx.learning.dto.request;

public record ChooseQuizAnswerRequest(
        String questionId,
        String userAnswer
) {
}
