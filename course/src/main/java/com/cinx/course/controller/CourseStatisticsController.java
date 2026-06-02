package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.response.AdminCourseStatisticsOverviewResponse;
import com.cinx.course.dto.response.InstructorCourseStatisticsOverviewResponse;
import com.cinx.course.service.statistics.ICourseStatisticsService;
import com.cinx.course.service.statistics.StatisticsGroupBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class CourseStatisticsController {
    private final ICourseStatisticsService courseStatisticsService;

    @GetMapping("/api/v1/admin/courses/statistics/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin course statistics overview", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<AdminCourseStatisticsOverviewResponse>> getAdminOverview(
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Admin course statistics fetched successfully",
                courseStatisticsService.getAdminOverview(groupBy, startDate, endDate)
        ));
    }

    @GetMapping("/api/v1/courses/mine/statistics/overview")
    @Operation(summary = "Get my course statistics overview", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<InstructorCourseStatisticsOverviewResponse>> getInstructorOverview(
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Instructor course statistics fetched successfully",
                courseStatisticsService.getInstructorOverview(groupBy, startDate, endDate)
        ));
    }
}
