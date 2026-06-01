package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record InstructorRevenueResponse(
        @Schema(example = "350000000")
        Long totalRevenue,
        List<RevenueByTimeResponse> revenueByMonth,
        List<CourseRevenueResponse> courseRevenues
) {
}
