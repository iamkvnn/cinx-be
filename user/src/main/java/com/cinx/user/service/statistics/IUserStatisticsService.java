package com.cinx.user.service.statistics;

import com.cinx.user.dto.response.UserStatisticsOverviewResponse;

import java.time.LocalDate;

public interface IUserStatisticsService {
    UserStatisticsOverviewResponse getOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);
}
