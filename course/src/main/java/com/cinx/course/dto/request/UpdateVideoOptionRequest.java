package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateVideoOptionRequest(
        String id,
        @NotBlank
        String optionText,
        Boolean isCorrect
) {
}