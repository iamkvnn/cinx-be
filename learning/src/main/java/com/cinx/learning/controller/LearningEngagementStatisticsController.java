package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.response.CourseEngagementOverviewResponse;
import com.cinx.learning.service.activity.LearningActivityGroupBy;
import com.cinx.learning.service.statistics.ILearningEngagementStatisticsService;
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
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LearningEngagementStatisticsController {
    private final ILearningEngagementStatisticsService statisticsService;

    @GetMapping("/courses/{courseId}/engagement/overview")
    @Operation(summary = "Get instructor course engagement overview", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<CourseEngagementOverviewResponse>> getInstructorCourseEngagement(
            @PathVariable String courseId,
            @RequestParam(required = false) LearningActivityGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course engagement overview fetched successfully",
                statisticsService.getInstructorCourseEngagement(courseId, groupBy, startDate, endDate)
        ));
    }

    @GetMapping("/admin/courses/{courseId}/engagement/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin course engagement overview", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<CourseEngagementOverviewResponse>> getAdminCourseEngagement(
            @PathVariable String courseId,
            @RequestParam(required = false) LearningActivityGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course engagement overview fetched successfully",
                statisticsService.getAdminCourseEngagement(courseId, groupBy, startDate, endDate)
        ));
    }
}
