package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AdminOverviewResponse(
        @Schema(example = "50000000")
        Long totalGrossRevenue,
        @Schema(example = "5000000")
        Long totalPlatformFeeRevenue,
        @Schema(example = "1200")
        Long totalOrders,
        List<RevenueByTimeResponse> platformRevenueByTime,
        List<CourseStats> topCourses
) {
}