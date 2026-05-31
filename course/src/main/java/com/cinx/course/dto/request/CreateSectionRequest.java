package com.cinx.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateSectionRequest(
        @NotBlank
        @Schema(example = "Introduction to Spring Boot")
        String title,
        @Schema(example = "Basic concepts and setup")
        String description,
        @Min(0)
        @Schema(example = "3600")
        Long duration
) {
}
