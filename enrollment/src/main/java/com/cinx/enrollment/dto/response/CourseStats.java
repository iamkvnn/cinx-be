package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CourseStats(
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "Advanced Java Principles")
        String title,
        @Schema(example = "450")
        Long enrollmentCount
) {}
