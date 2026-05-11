package com.cinx.enrollment.dto.response;

public record RevenueByTimeResponse(
        String timeLabel, // "YYYY-MM-DD" or "YYYY-MM" or "YYYY"
        Long grossRevenue,
        Long netRevenue
) {
}