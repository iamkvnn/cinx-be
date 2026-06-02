package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.response.CourseQnAStatisticsResponse;
import com.cinx.social.dto.response.ReportStatisticsOverviewResponse;
import com.cinx.social.dto.response.ReviewStatisticsResponse;
import com.cinx.social.service.statistics.ISocialStatisticsService;
import com.cinx.social.service.statistics.StatisticsGroupBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class SocialStatisticsController {
    private final ISocialStatisticsService socialStatisticsService;

    @GetMapping("/api/v1/reviews/statistics/courses/{courseId}")
    @Operation(summary = "Get course review statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<ReviewStatisticsResponse>> getReviewStatistics(@PathVariable String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Review statistics fetched successfully",
                socialStatisticsService.getReviewStatistics(courseId)
        ));
    }

    @GetMapping("/api/v1/course-qna/statistics/courses/{courseId}")
    @Operation(summary = "Get course QnA statistics", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<CourseQnAStatisticsResponse>> getCourseQnAStatistics(
            @PathVariable String courseId,
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course QnA statistics fetched successfully",
                socialStatisticsService.getCourseQnAStatistics(courseId, groupBy, startDate, endDate)
        ));
    }

    @GetMapping("/api/v1/reports/statistics/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get report statistics overview", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<ReportStatisticsOverviewResponse>> getReportOverview(
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Report statistics fetched successfully",
                socialStatisticsService.getReportOverview(groupBy, startDate, endDate)
        ));
    }
}
