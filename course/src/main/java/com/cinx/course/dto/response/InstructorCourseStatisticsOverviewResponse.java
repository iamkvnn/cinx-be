package com.cinx.course.dto.response;

import java.util.List;

public record InstructorCourseStatisticsOverviewResponse(
        Long createdCoursesInRange,
        List<StatisticsByTimeResponse> createdCoursesByTime,
        Long currentCourseCount,
        Long currentPublishedCourseCount,
        Double averageRating,
        Long currentEnrollmentSnapshot
) {
}
