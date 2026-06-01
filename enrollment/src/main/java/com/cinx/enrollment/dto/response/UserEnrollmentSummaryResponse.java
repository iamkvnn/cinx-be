package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserEnrollmentSummaryResponse(
        @Schema(example = "12")
        Long enrolledCourseCount,
        @Schema(example = "4500000")
        Long totalSpent,
        @Schema(example = "5")
        Long paidOrderCount
) {
}
