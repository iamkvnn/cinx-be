package com.cinx.learning.service.learningProgress;

import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.CourseDetailResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.mapper.CourseProgressMapper;
import com.cinx.learning.mapper.LearningItemProgressMapper;
import com.cinx.learning.model.CourseProgress;
import com.cinx.learning.model.LearningItemProgress;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningItemProgressRepository;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import com.cinx.learning.service.learningPath.ILearningPathService;
import com.cinx.learning.service.streak.IStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningProgressService implements ILearningProgressService{       
    private final CourseProgressRepository courseProgressRepository;
    private final LearningItemProgressRepository learningItemProgressRepository;
    private final CourseProgressMapper courseProgressMapper;
    private final LearningItemProgressMapper learningItemProgressMapper;        
    private final CourseService courseService;
    private final ILearningPathService learningPathService;
    private final IStreakService streakService;
    private final IDailyGoalService dailyGoalService;

    @Override
    public List<CourseProgressResponse> getCourseProgressByCourseIds(String userId, List<String> courseIds) {
        return courseProgressRepository.findAllByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .map(courseProgressMapper::toDto)
                .toList();
    }

    @Override
    public CourseProgressResponse getCourseProgress(String userId, String courseId) {
        return courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .map(courseProgressMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Course progress not found"));
    }

    @Override
    public List<LearningItemProgressResponse> getLearningItemProgressByCourseId(String userId, String courseId) {
        return learningItemProgressRepository.findAllByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(learningItemProgressMapper::toDto)
                .toList();
    }

    @Override
    public List<CourseProgressResponse> getCourseProgressByCourseId(String courseId) {
        return courseProgressRepository.findAllByCourseId(courseId)
                .stream()
                .map(courseProgressMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void createCourseProgress(String userId, String courseId) {
        CourseDetailResponse courseDetail = courseService.getCourseById(courseId).data();
        CourseProgress courseProgress = courseProgressRepository.save(
                CourseProgress.builder()
                        .isCompleted(false)
                        .userId(userId)
                        .courseId(courseId)
                        .totalItems(courseDetail.sections().stream()
                                .flatMap(section -> section.lessons().stream())
                                .mapToInt(lesson -> 1)
                                .sum())
                        .completedItems(0)
                        .build()
        );
        learningItemProgressRepository.saveAll(courseDetail.sections().stream()
                .flatMap(section -> section.lessons().stream())
                .map(lesson -> LearningItemProgress.builder()
                        .isCompleted(false)
                        .courseProgress(courseProgress)
                        .itemId(lesson.id())
                        .build())
                .toList());
    }

    @Transactional
    @Override
    public void updateLearningItemProgress(String userId, String itemId, UpdateLearningItemRequest request) {
        LearningItemProgress progress = learningItemProgressRepository
                .findByItemIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Learning item progress not found"));

        CourseProgress course = progress.getCourseProgress();
        Boolean oldCompleted = progress.getIsCompleted();
        Double oldScore = progress.getScore();
        Boolean newCompleted = request.isCompleted() != null ? request.isCompleted() : oldCompleted;
        Double newScore = request.score();

        if (oldScore != null && newScore <= oldScore) {
            return;
        }

        int completedItems = course.getCompletedItems() != null ? course.getCompletedItems() : 0;
        double totalScore = (course.getAvgScore() != null ? course.getAvgScore() : 0.0) * completedItems;

        if (!Boolean.TRUE.equals(oldCompleted) && Boolean.TRUE.equals(newCompleted)) {
            completedItems++;
            totalScore += newScore;
            progress.setIsCompleted(true);
            progress.setScore(newScore);
            progress.setIsPassed(request.isPassed());
            streakService.updateStreakOnActivity(userId);
            dailyGoalService.addXp(userId, 50);
            learningPathService.updatePathProgress(userId, itemId);
        } else if (Boolean.TRUE.equals(oldCompleted) && Boolean.TRUE.equals(newCompleted)) {
            totalScore = totalScore - (oldScore != null ? oldScore : 0.0) + newScore;
            progress.setScore(newScore);
        }

        course.setCompletedItems(completedItems);
        course.setAvgScore(completedItems > 0 ? totalScore / completedItems : 0.0);

        if (course.getTotalItems() != null && completedItems == course.getTotalItems()) {
            course.setIsCompleted(true);
            course.setCompletionTime(LocalDateTime.now());
        }

        learningItemProgressRepository.save(progress);
        courseProgressRepository.save(course);
    }
}
