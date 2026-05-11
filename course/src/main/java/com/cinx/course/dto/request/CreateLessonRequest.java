package com.cinx.course.dto.request;

import com.cinx.course.consts.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateLessonRequest (
     @NotBlank(message = "Title is required")
     String title,
     @Min(value = 0, message = "Duration must not be negative")
     Long duration,
     @NotNull(message = "Order index is required")
     Integer orderIndex,
     @NotNull(message = "Lesson type is required")
     LessonType lessonType,
     @NotNull(message = "Preview status is required")
     Boolean isPreview,
     List<String> prerequisiteIds
) {
}

