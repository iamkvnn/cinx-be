package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record InstructorStatisticsResponse(
        @Schema(example = "15000000")
        Long totalGrossRevenue,
        @Schema(example = "12000000")
        Long totalNetRevenue, // After platform fee deduction
        List<RevenueByTimeResponse> revenueByTime,
        List<CourseStats> topCourses
) {
}