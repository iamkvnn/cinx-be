package com.cinx.user.dto.response;

import java.util.List;
import java.util.Map;

public record UserStatisticsOverviewResponse(
        Long newUsersInRange,
        Map<String, Long> usersByRole,
        Map<String, Long> instructorsByVerificationStatus,
        List<StatisticsByTimeResponse> newUsersByTime,
        Long currentTotalUsers
) {
}
