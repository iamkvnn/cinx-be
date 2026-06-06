package com.cinx.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItemRequest {
    @Schema(example = "course_123")
    @NotBlank(message = "courseId must not be blank")
    private String courseId;
    @Schema(example = "lesson_123")
    @NotBlank(message = "lessonId must not be blank")
    private String lessonId;
    @Schema(example = "1")
    private Integer orderIndex;
    @Schema(example = "true")
    private Boolean isSuggested;
}
