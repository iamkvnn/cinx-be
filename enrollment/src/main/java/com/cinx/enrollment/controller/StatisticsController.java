package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.enrollment.dto.response.AdminOverviewResponse;
import com.cinx.enrollment.dto.response.DashboardMetricsResponse;
import com.cinx.enrollment.dto.response.InstructorStatisticsResponse;
import com.cinx.enrollment.dto.response.CourseStatisticsResponse;
import com.cinx.enrollment.service.statistics.IStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Get yearly instructor statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/instructor/overview/yearly")
    public ResponseEntity<ApiResponse<InstructorStatisticsResponse>> getInstructorYearlyOverview(
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor yearly statistics fetched successfully", statisticsService.getInstructorYearlyOverview(year))
        );
    }

    @Operation(summary = "Get monthly instructor statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/instructor/overview/monthly")
    public ResponseEntity<ApiResponse<InstructorStatisticsResponse>> getInstructorMonthlyOverview(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor monthly statistics fetched successfully", statisticsService.getInstructorMonthlyOverview(year, month))
        );
    }

    @Operation(summary = "Get range instructor statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/instructor/overview/range")
    public ResponseEntity<ApiResponse<InstructorStatisticsResponse>> getInstructorRangeOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor range statistics fetched successfully", statisticsService.getInstructorRangeOverview(startDate, endDate))
        );
    }

    @Operation(summary = "Get course specific statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/instructor/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseStatisticsResponse>> getCourseStatistics(
            @PathVariable String courseId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course statistics fetched successfully", statisticsService.getCourseStatistics(courseId, year, month, startDate, endDate))
        );
    }

    @Operation(summary = "Get admin yearly overview", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/admin/overview/yearly")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getAdminYearlyOverview(
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Admin yearly overview fetched successfully", statisticsService.getAdminYearlyOverview(year))
        );
    }

    @Operation(summary = "Get admin monthly overview", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/admin/overview/monthly")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getAdminMonthlyOverview(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Admin monthly overview fetched successfully", statisticsService.getAdminMonthlyOverview(year, month))
        );
    }

    @Operation(summary = "Get admin range overview", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/admin/overview/range")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getAdminRangeOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Admin range overview fetched successfully", statisticsService.getAdminRangeOverview(startDate, endDate))
        );
    }
}
