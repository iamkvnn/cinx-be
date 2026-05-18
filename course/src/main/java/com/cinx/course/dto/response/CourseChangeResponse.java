package com.cinx.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CourseChangeResponse(
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "tit_123")
        String itemId,
        @Schema(example = "Old Title")
        String oldValue,
        @Schema(example = "New Title")
        String newValue
) {
}
