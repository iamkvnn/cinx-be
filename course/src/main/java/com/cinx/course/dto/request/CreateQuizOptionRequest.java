package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuizOptionRequest(
        @NotBlank
        String optionText,
        @NotNull
        Boolean isCorrect,
        Integer optionOrder,
        String matchText
) {
}
