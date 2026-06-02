package com.cinx.social.service.statistics;

import com.cinx.social.dto.response.CourseQnAStatisticsResponse;
import com.cinx.social.dto.response.ReportStatisticsOverviewResponse;
import com.cinx.social.dto.response.ReviewStatisticsResponse;

import java.time.LocalDate;

public interface ISocialStatisticsService {
    ReviewStatisticsResponse getReviewStatistics(String courseId);

    CourseQnAStatisticsResponse getCourseQnAStatistics(String courseId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);

    ReportStatisticsOverviewResponse getReportOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);
}
