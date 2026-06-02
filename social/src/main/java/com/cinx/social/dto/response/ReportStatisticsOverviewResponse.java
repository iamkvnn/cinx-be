package com.cinx.social.dto.response;

import java.util.List;
import java.util.Map;

public record ReportStatisticsOverviewResponse(
        Long reportsInRange,
        Map<String, Long> reportsByType,
        List<StatisticsByTimeResponse> reportsByTime,
        List<TopReportedRefResponse> topReportedRefs
) {
}
