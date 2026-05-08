package com.cinx.course.dto.request;

public record UpdateQuizOptionRequest(
        String id,
        String optionText,
        Boolean isCorrect,
        Integer optionOrder,
        String matchText
) {}
