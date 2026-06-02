package com.cinx.learning.service.statistics;

import com.cinx.learning.dto.response.CourseEngagementOverviewResponse;
import com.cinx.learning.service.activity.LearningActivityGroupBy;

import java.time.LocalDate;

public interface ILearningEngagementStatisticsService {
    CourseEngagementOverviewResponse getInstructorCourseEngagement(String courseId, LearningActivityGroupBy groupBy, LocalDate startDate, LocalDate endDate);

    CourseEngagementOverviewResponse getAdminCourseEngagement(String courseId, LearningActivityGroupBy groupBy, LocalDate startDate, LocalDate endDate);
}
