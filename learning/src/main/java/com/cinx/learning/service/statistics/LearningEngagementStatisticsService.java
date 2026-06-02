package com.cinx.learning.service.statistics;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.response.CourseDetailResponse;
import com.cinx.learning.dto.response.CourseEngagementOverviewResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningActivityByTimeResponse;
import com.cinx.learning.model.CourseProgress;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningActivityDailyRepository;
import com.cinx.learning.service.activity.LearningActivityDateRange;
import com.cinx.learning.service.activity.LearningActivityGroupBy;
import com.cinx.learning.service.activity.LearningActivityRangeResolver;
import com.cinx.learning.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningEngagementStatisticsService implements ILearningEngagementStatisticsService {
    private final LearningActivityDailyRepository learningActivityDailyRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseService courseService;
    private final LearningActivityRangeResolver activityRangeResolver = new LearningActivityRangeResolver();

    @Override
    @Transactional(readOnly = true)
    public CourseEngagementOverviewResponse getInstructorCourseEngagement(String courseId, LearningActivityGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        CourseDetailResponse course = courseService.getCourseById(courseId).data();
        String currentUserId = AuthenticationUtil.extractUserId();
        if (course.instructor() == null || !currentUserId.equals(course.instructor().id())) {
            throw new BadRequestException("Only the course instructor can view this engagement overview");
        }
        return buildCourseEngagement(courseId, groupBy, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseEngagementOverviewResponse getAdminCourseEngagement(String courseId, LearningActivityGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        return buildCourseEngagement(courseId, groupBy, startDate, endDate);
    }

    private CourseEngagementOverviewResponse buildCourseEngagement(String courseId, LearningActivityGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        LearningActivityDateRange range = activityRangeResolver.resolve(groupBy, startDate, endDate);
        List<Object[]> activityRows = range.groupByDay()
                ? learningActivityDailyRepository.aggregateCourseActivityByDay(courseId, range.startDate(), range.endDate())
                : learningActivityDailyRepository.aggregateCourseActivityByMonth(courseId, range.startDate(), range.endDate());
        Long totalLearningSeconds = learningActivityDailyRepository.sumActiveSecondsByCourseIdBetween(courseId, range.startDate(), range.endDate());
        Long activeLearners = learningActivityDailyRepository.countActiveLearnersByCourseIdBetween(courseId, range.startDate(), range.endDate());
        List<CourseProgress> progressList = courseProgressRepository.findAllByCourseId(courseId);
        return new CourseEngagementOverviewResponse(
                activeLearners != null ? activeLearners : 0L,
                totalLearningSeconds != null ? totalLearningSeconds : 0L,
                averageProgressPercent(progressList),
                completionRate(progressList),
                fillActivityByTime(range, activityRows)
        );
    }

    private List<LearningActivityByTimeResponse> fillActivityByTime(LearningActivityDateRange range, List<Object[]> rows) {
        Map<String, Long> valuesByLabel = new LinkedHashMap<>();
        range.bucketLabels().forEach(label -> valuesByLabel.put(label, 0L));
        rows.forEach(row -> valuesByLabel.put((String) row[0], ((Number) row[1]).longValue()));
        return valuesByLabel.entrySet().stream()
                .map(entry -> new LearningActivityByTimeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Double averageProgressPercent(List<CourseProgress> progressList) {
        return progressList.stream()
                .mapToDouble(this::progressPercent)
                .average()
                .orElse(0.0);
    }

    private Double completionRate(List<CourseProgress> progressList) {
        if (progressList.isEmpty()) {
            return 0.0;
        }
        long completedCount = progressList.stream()
                .filter(progress -> Boolean.TRUE.equals(progress.getIsCompleted()))
                .count();
        return completedCount * 100.0 / progressList.size();
    }

    private double progressPercent(CourseProgress progress) {
        if (progress.getTotalItems() == null || progress.getTotalItems() <= 0) {
            return 0.0;
        }
        int completedItems = progress.getCompletedItems() != null ? progress.getCompletedItems() : 0;
        return completedItems * 100.0 / progress.getTotalItems();
    }
}
