package com.cinx.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RejectCourseResponse(
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "Course content violates copyright policies.")
        String reason
) {
}
