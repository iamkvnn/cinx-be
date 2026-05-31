package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItemResponse {
    @Schema(example = "path_item_123")
    private String id;
    @Schema(example = "course_123")
    private String courseId;
    @Schema(example = "lesson_123")
    private String lessonId;
    @Schema(example = "1")
    private Integer orderIndex;
    @Schema(example = "true")
    private Boolean isSuggested;
    @Schema(example = "true")
    private Boolean isCompleted;
}
