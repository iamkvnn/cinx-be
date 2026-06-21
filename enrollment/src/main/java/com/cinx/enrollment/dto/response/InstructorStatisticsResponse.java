package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record InstructorStatisticsResponse(
        @Schema(example = "15000000")
        Long totalGrossRevenue,
        @Schema(example = "12000000")
        Long totalNetRevenue, // After platform fee deduction
        @Schema(example = "120")
        Long enrollmentsInRange,
        @Schema(example = "80")
        Long distinctLearnersInRange,
        List<RevenueByTimeResponse> revenueByTime,
        List<EnrollmentByTimeResponse> enrollmentsByTime,
        List<CourseRevenueStats> topCoursesByRevenue,
        List<CourseStats> topCoursesByEnrollment
) {
}
