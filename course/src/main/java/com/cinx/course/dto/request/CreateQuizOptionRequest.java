package com.cinx.course.dto.request;

public record CreateQuizOptionRequest(
        String optionText,
        Boolean isCorrect,
        Integer optionOrder
) {
}
