package com.cinx.course.service.statistics;

import com.cinx.course.dto.response.AdminCourseStatisticsOverviewResponse;
import com.cinx.course.dto.response.InstructorCourseStatisticsOverviewResponse;

import java.time.LocalDate;

public interface ICourseStatisticsService {
    AdminCourseStatisticsOverviewResponse getAdminOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);

    InstructorCourseStatisticsOverviewResponse getInstructorOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate);
}
