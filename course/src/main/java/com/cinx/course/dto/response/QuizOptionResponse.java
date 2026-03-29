package com.cinx.course.dto.response;

public record QuizOptionResponse (
        String optionText,
        Boolean isCorrect,
        Integer optionOrder
) {}
