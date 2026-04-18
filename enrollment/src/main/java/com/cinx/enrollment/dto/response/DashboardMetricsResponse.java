package com.cinx.enrollment.dto.response;

import java.util.List;

public record DashboardMetricsResponse(
        Long totalRevenue,
        Long totalUsers,
        Long newUsersThisMonth,
        List<CourseStats> topEnrolledCourses,
        Long paidOrdersThisMonth
) {}
