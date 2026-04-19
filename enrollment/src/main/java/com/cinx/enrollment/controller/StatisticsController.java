package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.enrollment.dto.response.DashboardMetricsResponse;
import com.cinx.enrollment.service.statistics.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

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
}
