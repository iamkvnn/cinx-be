package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateQuizOptionRequest(
        String id,
        @NotBlank
        String optionText,
        @NotNull
        Boolean isCorrect,
        Integer optionOrder,
        String matchText
) {}
