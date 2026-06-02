package com.cinx.enrollment.service.statistics;

import java.time.LocalDateTime;
import java.util.List;

public record StatisticsDateRange(
        LocalDateTime start,
        LocalDateTime end,
        StatisticsGroupBy groupBy,
        List<String> bucketLabels
) {
    public boolean groupByDay() {
        return groupBy == StatisticsGroupBy.DAY;
    }
}
