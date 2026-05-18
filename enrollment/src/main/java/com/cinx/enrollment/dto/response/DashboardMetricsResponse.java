package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DashboardMetricsResponse(
        @Schema(example = "50000000")
        Long totalRevenue,
        @Schema(example = "1500")
        Long totalUsers,
        @Schema(example = "120")
        Long newUsersThisMonth,
        List<CourseStats> topEnrolledCourses,
        @Schema(example = "85")
        Long paidOrdersThisMonth
) {}
