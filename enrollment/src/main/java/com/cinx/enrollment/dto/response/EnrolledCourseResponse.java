package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record EnrolledCourseResponse(
        CourseResponse course,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime enrolledAt
) {
}
