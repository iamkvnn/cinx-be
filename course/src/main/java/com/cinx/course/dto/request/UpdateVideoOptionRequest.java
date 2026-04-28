package com.cinx.course.dto.request;

public record UpdateVideoOptionRequest(
        String id,
        String optionText,
        Boolean isCorrect,
        Integer optionOrder
) {
}