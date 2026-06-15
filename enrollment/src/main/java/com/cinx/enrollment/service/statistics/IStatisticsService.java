package com.cinx.enrollment.service.statistics;

import com.cinx.enrollment.dto.response.AdminOverviewResponse;
import com.cinx.enrollment.dto.response.DashboardMetricsResponse;
import com.cinx.enrollment.dto.response.InstructorRevenueResponse;
import com.cinx.enrollment.dto.response.InstructorStatisticsResponse;

import com.cinx.enrollment.dto.response.CourseStatisticsResponse;
import java.time.LocalDate;

public interface IStatisticsService {
    DashboardMetricsResponse getDashboardMetrics(Integer year, Integer month);

    InstructorStatisticsResponse getInstructorOverview(String instructorId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);

    AdminOverviewResponse getAdminOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);

    CourseStatisticsResponse getCourseStatistics(String instructorId, String courseId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);

    InstructorRevenueResponse getInstructorRevenueSeries(String instructorId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);
}
