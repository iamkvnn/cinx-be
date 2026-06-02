package com.cinx.user.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.user.dto.response.UserStatisticsOverviewResponse;
import com.cinx.user.service.statistics.IUserStatisticsService;
import com.cinx.user.service.statistics.StatisticsGroupBy;
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
@RequestMapping("/api/v1/users/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserStatisticsController {
    private final IUserStatisticsService userStatisticsService;

    @GetMapping("/overview")
    @Operation(summary = "Get user statistics overview", security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<UserStatisticsOverviewResponse>> getOverview(
            @RequestParam(required = false) StatisticsGroupBy groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User statistics fetched successfully",
                userStatisticsService.getOverview(groupBy, startDate, endDate)
        ));
    }
}
