package com.cinx.learning.service.instructor;

import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.dto.response.QuizQuestionAnalyticsResponse;

import java.util.List;

public interface IInstructorService {
    List<CourseProgressResponse> getCourseProgressByCourseId(String courseId);
    List<LearningItemProgressResponse> getStudentProgressByCourseIdAndStudentId(String courseId, String studentId);
    List<QuizQuestionAnalyticsResponse> getQuizAnalytics(String quizId);
}