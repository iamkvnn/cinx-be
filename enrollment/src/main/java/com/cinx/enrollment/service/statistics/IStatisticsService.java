package com.cinx.enrollment.service.statistics;

import com.cinx.enrollment.dto.response.AdminOverviewResponse;
import com.cinx.enrollment.dto.response.DashboardMetricsResponse;
import com.cinx.enrollment.dto.response.InstructorRevenueResponse;
import com.cinx.enrollment.dto.response.InstructorStatisticsResponse;

import com.cinx.enrollment.dto.response.CourseStatisticsResponse;
import java.time.LocalDate;

public interface IStatisticsService {
    DashboardMetricsResponse getDashboardMetrics(Integer year, Integer month);

    InstructorStatisticsResponse getInstructorYearlyOverview(Integer year);
    InstructorStatisticsResponse getInstructorMonthlyOverview(Integer year, Integer month);
    InstructorStatisticsResponse getInstructorRangeOverview(LocalDate startDate, LocalDate endDate);

    AdminOverviewResponse getAdminYearlyOverview(Integer year);
    AdminOverviewResponse getAdminMonthlyOverview(Integer year, Integer month);
    AdminOverviewResponse getAdminRangeOverview(LocalDate startDate, LocalDate endDate);

    CourseStatisticsResponse getCourseStatistics(String courseId, Integer year, Integer month, LocalDate startDate, LocalDate endDate);

    InstructorRevenueResponse getInstructorRevenue(String instructorId, Integer months);
}
