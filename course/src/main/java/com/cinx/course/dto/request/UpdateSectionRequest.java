package com.cinx.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateSectionRequest(
        @NotBlank
        String title,
        String description,
        @Min(0)
        Long duration,
        @Min(0)
        Integer orderIndex
) {
}
