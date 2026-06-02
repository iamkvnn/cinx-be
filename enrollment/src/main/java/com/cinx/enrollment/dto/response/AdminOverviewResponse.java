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
        @Schema(example = "350")
        Long enrollmentsInRange,
        @Schema(example = "300")
        Long paidOrdersInRange,
        @Schema(example = "220")
        Long distinctLearnersInRange,
        List<RevenueByTimeResponse> platformRevenueByTime,
        List<EnrollmentByTimeResponse> enrollmentsByTime,
        List<CourseStats> topCoursesByRevenue,
        List<CourseStats> topCoursesByEnrollment
) {
}
