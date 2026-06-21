package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CourseRevenueStats(
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "Advanced Java Principles")
        String title,
        @Schema(example = "899000")
        Long revenue
) {
}
