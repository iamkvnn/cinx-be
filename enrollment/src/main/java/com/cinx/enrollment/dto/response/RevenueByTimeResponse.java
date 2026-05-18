package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RevenueByTimeResponse(
        @Schema(example = "2025-01-01")
        String timeLabel, // "YYYY-MM-DD" or "YYYY-MM" or "YYYY"
        @Schema(example = "2000000")
        Long grossRevenue,
        @Schema(example = "1800000")
        Long netRevenue
) {
}