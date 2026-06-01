package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CourseRevenueResponse(
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "Advanced Java")
        String title,
        @Schema(example = "450")
        Long enrollmentCount,
        @Schema(example = "150000000")
        Long revenue
) {
}
