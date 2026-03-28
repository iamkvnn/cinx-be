package com.cinx.learning.dto.response;

public record QuizOptionResponse(
        String optionText,
        Boolean isCorrect,
        Integer optionOrder
) {}
