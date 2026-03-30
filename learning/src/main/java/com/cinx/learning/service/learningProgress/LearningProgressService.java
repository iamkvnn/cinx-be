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
        learningItemProgressRepository.findByItemIdAndUserId(itemId, userId)
                .ifPresentOrElse(
                        (existingProgress) -> {
                            learningItemProgressMapper.partialUpdate(existingProgress, request);
                            if (request.isCompleted() != null && request.isCompleted() && !existingProgress.getIsCompleted()) {
                                CourseProgress courseProgress = existingProgress.getCourseProgress();
                                courseProgress.setCompletedItems(courseProgress.getCompletedItems() + 1);
                                if (request.score() != null) {
                                    courseProgress.setAvgScore(courseProgress.getAvgScore() == null
                                            ? request.score()
                                            : (courseProgress.getAvgScore() * (courseProgress.getCompletedItems() - 1) + request.score()) / courseProgress.getCompletedItems());
                                }
                                if (courseProgress.getCompletedItems().equals(courseProgress.getTotalItems())) {
                                    courseProgress.setIsCompleted(true);
                                    courseProgress.setCompletionTime(LocalDateTime.now());
                                }
                                courseProgressRepository.save(courseProgress);
                            }
                            learningItemProgressRepository.save(existingProgress);
                        },
                        () -> {
                            throw new NotFoundException("Learning item progress not found");
                        }
                );
    }
}
