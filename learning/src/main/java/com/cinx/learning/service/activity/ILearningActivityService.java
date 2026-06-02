package com.cinx.learning.service.activity;

import com.cinx.learning.dto.request.LearningActivityRequest;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.CoursesProgressSummaryResponse;
import com.cinx.learning.dto.response.LearningActivityByTimeResponse;
import com.cinx.learning.dto.response.UserLearningSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ILearningActivityService {
    void recordActivity(String userId, LearningActivityRequest request);
    void recordActivity(String userId, String courseId, Integer activeSeconds);
    UserLearningSummaryResponse getUserLearningSummary(String userId);
    List<CourseProgressResponse> getUserCourseProgress(String userId, List<String> courseIds);
    List<LearningActivityByTimeResponse> getUserActivitySeries(String userId, LearningActivityGroupBy groupBy, LocalDate startDate, LocalDate endDate);
    CoursesProgressSummaryResponse getCoursesProgressSummary(List<String> courseIds);
}
