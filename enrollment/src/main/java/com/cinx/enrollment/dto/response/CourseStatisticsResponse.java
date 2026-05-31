package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CourseStatisticsResponse(
        @Schema(example = "10000000")
        Long totalGrossRevenue,
        @Schema(example = "8000000")
        Long totalNetRevenue,
        List<RevenueByTimeResponse> revenueByTime
) {
}