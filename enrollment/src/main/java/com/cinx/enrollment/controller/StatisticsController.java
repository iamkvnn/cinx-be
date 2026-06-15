package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.dto.response.AdminOverviewResponse;
import com.cinx.enrollment.dto.response.DashboardMetricsResponse;
import com.cinx.enrollment.dto.response.InstructorRevenueResponse;
import com.cinx.enrollment.dto.response.InstructorStatisticsResponse;
import com.cinx.enrollment.dto.response.CourseStatisticsResponse;
import com.cinx.enrollment.service.statistics.IStatisticsService;
import com.cinx.enrollment.service.statistics.StatisticsGroupBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @Operation(summary = "Get dashboard metrics", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboardMetrics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Dashboard metrics fetched successfully", statisticsService.getDashboardMetrics(year, month))
        );
    }

    @Operation(summary = "Get instructor statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/instructor/overview")
    public ResponseEntity<ApiResponse<InstructorStatisticsResponse>> getInstructorOverview(
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor statistics fetched successfully", statisticsService.getInstructorOverview(AuthenticationUtil.extractUserId(), groupBy, startDate, endDate))
        );
    }

    @Operation(summary = "Get course specific statistics overview", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/instructor/courses/{courseId}/overview")
    public ResponseEntity<ApiResponse<CourseStatisticsResponse>> getCourseStatisticsOverview(
            @PathVariable String courseId,
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course statistics fetched successfully", statisticsService.getCourseStatistics(AuthenticationUtil.extractUserId(), courseId, groupBy, startDate, endDate))
        );
    }

    @Operation(summary = "Get admin overview", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/admin/overview")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getAdminOverview(
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Admin overview fetched successfully", statisticsService.getAdminOverview(groupBy, startDate, endDate))
        );
    }

    @Operation(summary = "Get admin instructor revenue series", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/admin/instructors/{instructorId}/revenue/series")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InstructorRevenueResponse>> getInstructorRevenueSeries(
            @PathVariable String instructorId,
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor revenue fetched successfully", statisticsService.getInstructorRevenueSeries(instructorId, groupBy, startDate, endDate))
        );
    }

}
