package com.cinx.course.dto.request;

public record CreateVideoOptionRequest(
        String optionText,
        Boolean isCorrect,
        Integer optionOrder
) {
}
