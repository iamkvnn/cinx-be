package com.cinx.learning.service.learningProgress;

import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;

import java.util.List;

public interface ILearningProgressService {
    List<CourseProgressResponse> getCourseProgressByCourseIds(String userId, List<String> courseIds);
    CourseProgressResponse getCourseProgress(String userId, String courseId);
    List<LearningItemProgressResponse> getLearningItemProgressByCourseId(String userId, String courseId);
    List<CourseProgressResponse> getCourseProgressByCourseId(String courseId);
    void createCourseProgress(String userId, String courseId);
    void updateLearningItemProgress(String userId, String itemId, UpdateLearningItemRequest request);
    void recomputeCourseProgress(String courseId, String lessonId, String changeType);
}
