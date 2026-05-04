package com.cinx.learning.dto.response;

public record QuizOptionResponse(
        String id,
        String optionText,
        Boolean isCorrect,
        Integer optionOrder,
        String matchText
) {}
