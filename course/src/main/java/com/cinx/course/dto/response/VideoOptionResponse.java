package com.cinx.course.dto.response;

public record VideoOptionResponse(
        String id,
        String optionText,
        Boolean isCorrect
) {
}