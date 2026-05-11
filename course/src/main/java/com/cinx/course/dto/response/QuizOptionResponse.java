package com.cinx.course.dto.response;

public record QuizOptionResponse (
        String id,
        String optionText,
        Boolean isCorrect,
        Integer optionOrder,
        String matchText
) {}
