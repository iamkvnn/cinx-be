package com.cinx.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVideoNoteRequest {
    @Schema(example = "course_123")
    @NotBlank(message = "Course ID is required")
    private String courseId;

    @Schema(example = "lesson_123")
    @NotBlank(message = "Lesson ID is required")
    private String lessonId;

    @Schema(example = "This is a very important point.")
    @NotBlank(message = "Content is required")
    private String content;

    @Schema(example = "120")
    @NotNull(message = "Video timestamp is required")
    @Min(value = 0, message = "Video timestamp must be non-negative")
    private Integer videoTimestamp;
}
