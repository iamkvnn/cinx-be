package com.cinx.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSectionRequest(
        @NotBlank
        String title,
        String description,
        @Min(0)
        Long duration,
        @NotNull
        Integer orderIndex
) {
}
