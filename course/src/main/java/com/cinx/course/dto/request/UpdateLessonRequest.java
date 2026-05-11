package com.cinx.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateLessonRequest(
     @NotBlank
     String title,
     @Min(0)
     Long duration,
     @Min(0)
     Integer orderIndex,
     Boolean isPreview,
     List<String> prerequisiteIds
) {
}

