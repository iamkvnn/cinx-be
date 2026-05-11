package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateVideoOptionRequest(
        @NotBlank
        String optionText,
        @NotNull
        Boolean isCorrect
) {
}
