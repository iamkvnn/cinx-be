package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RevenueByCourseResponse(
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "Java Programming 101")
        String title,
        @Schema(example = "150")
        Long enrollments,
        @Schema(example = "1500000")
        Long grossRevenue,
        @Schema(example = "1350000")
        Long netRevenue
) {
}