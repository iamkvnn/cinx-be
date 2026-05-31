package com.cinx.course.dto.request;

import com.cinx.course.consts.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateLessonRequest (
     @NotBlank(message = "Title is required")
     @Schema(example = "What is Spring Boot?")
     String title,
     @Min(value = 0, message = "Duration must not be negative")
     @Schema(example = "600")
     Long duration,
     @NotNull(message = "Lesson type is required")
     @Schema(example = "VIDEO")
     LessonType lessonType,
     @NotNull(message = "Preview status is required")
     @Schema(example = "true")
     Boolean isPreview,
     @Schema(example = "[\"les_122\"]")
     List<String> prerequisiteIds
) {
}

