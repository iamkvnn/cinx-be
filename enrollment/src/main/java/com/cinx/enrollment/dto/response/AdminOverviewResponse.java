package com.cinx.enrollment.dto.response;

import java.util.List;

public record AdminOverviewResponse(
        Long totalGrossRevenue,
        Long totalPlatformFeeRevenue,
        Long totalOrders,
        List<RevenueByTimeResponse> platformRevenueByTime,
        List<CourseStats> topCourses
) {
}