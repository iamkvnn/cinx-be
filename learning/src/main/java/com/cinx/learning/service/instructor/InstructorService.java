package com.cinx.learning.service.instructor;

import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.dto.response.QuizQuestionAnalyticsResponse;
import com.cinx.learning.mapper.CourseProgressMapper;
import com.cinx.learning.mapper.LearningItemProgressMapper;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningItemProgressRepository;
import com.cinx.learning.repository.QuizSessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorService implements IInstructorService {
    private final CourseProgressRepository courseProgressRepository;
    private final LearningItemProgressRepository learningItemProgressRepository;
    private final QuizSessionQuestionRepository quizSessionQuestionRepository;
    private final CourseProgressMapper courseProgressMapper;
    private final LearningItemProgressMapper learningItemProgressMapper;

    @Override
    public List<CourseProgressResponse> getCourseProgressByCourseId(String courseId) {
        return courseProgressRepository.findAllByCourseId(courseId)
                .stream()
                .map(courseProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningItemProgressResponse> getStudentProgressByCourseIdAndStudentId(String courseId, String studentId) {
        return learningItemProgressRepository.findAllByUserIdAndCourseId(studentId, courseId)
                .stream()
                .map(learningItemProgressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizQuestionAnalyticsResponse> getQuizAnalytics(String quizId) {
        return quizSessionQuestionRepository.getQuizAnalyticsByQuizId(quizId)
                .stream()
                .map(obj -> new QuizQuestionAnalyticsResponse(
                        (String) obj[0],
                        ((Number) obj[1]).intValue(),
                        ((Number) obj[2]).intValue(),
                        ((Number) obj[1]).doubleValue() == 0 ? 0.0 : ((Number) obj[2]).doubleValue() / ((Number) obj[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }
}