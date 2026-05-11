package com.cinx.enrollment.dto.response;

import java.util.List;

public record InstructorStatisticsResponse(
        Long totalGrossRevenue,
        Long totalNetRevenue, // After platform fee deduction
        List<RevenueByTimeResponse> revenueByTime,
        List<CourseStats> topCourses
) {
}