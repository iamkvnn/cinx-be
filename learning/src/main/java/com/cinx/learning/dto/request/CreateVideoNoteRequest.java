package com.cinx.learning.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVideoNoteRequest {
    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotBlank(message = "Lesson ID is required")
    private String lessonId;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Video timestamp is required")
    @Min(value = 0, message = "Video timestamp must be non-negative")
    private Integer videoTimestamp;
}
