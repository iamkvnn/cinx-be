package com.cinx.enrollment.dto.response;

import java.util.List;

public record CourseStatisticsResponse(
        Long totalGrossRevenue,
        Long totalNetRevenue,
        List<RevenueByTimeResponse> revenueByTime
) {
}