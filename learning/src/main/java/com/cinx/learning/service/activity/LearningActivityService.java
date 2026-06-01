package com.cinx.learning.service.activity;

import com.cinx.common.exception.BadRequestException;
import com.cinx.learning.dto.request.LearningActivityRequest;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.CourseProgressSummaryResponse;
import com.cinx.learning.dto.response.CoursesProgressSummaryResponse;
import com.cinx.learning.dto.response.LearningActivityByMonthResponse;
import com.cinx.learning.dto.response.UserLearningSummaryResponse;
import com.cinx.learning.mapper.CourseProgressMapper;
import com.cinx.learning.model.CourseProgress;
import com.cinx.learning.model.LearningActivityDaily;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningActivityDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningActivityService implements ILearningActivityService {
    private static final int MAX_HEARTBEAT_SECONDS = 300;

    private final LearningActivityDailyRepository learningActivityDailyRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseProgressMapper courseProgressMapper;

    @Override
    @Transactional
    public void recordActivity(String userId, LearningActivityRequest request) {
        recordActivity(userId, request.courseId(), request.activeSeconds());
    }

    @Override
    @Transactional
    public void recordActivity(String userId, String courseId, Integer activeSeconds) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestException("userId must not be blank");
        }
        if (courseId == null || courseId.isBlank()) {
            throw new BadRequestException("courseId must not be blank");
        }
        if (activeSeconds == null) {
            throw new BadRequestException("activeSeconds must not be null");
        }

        int cappedSeconds = Math.min(MAX_HEARTBEAT_SECONDS, Math.max(1, activeSeconds));
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LearningActivityDaily activity = learningActivityDailyRepository
                .findByUserIdAndCourseIdAndActivityDate(userId, courseId, today)
                .orElseGet(() -> LearningActivityDaily.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .activityDate(today)
                        .activeSeconds(0L)
                        .lastActivityAt(now)
                        .build());
        activity.setActiveSeconds((activity.getActiveSeconds() != null ? activity.getActiveSeconds() : 0L) + cappedSeconds);
        activity.setLastActivityAt(now);
        learningActivityDailyRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserLearningSummaryResponse getUserLearningSummary(String userId) {
        List<CourseProgress> progressList = courseProgressRepository.findAllByUserId(userId);
        long completedCourseCount = progressList.stream()
                .filter(progress -> Boolean.TRUE.equals(progress.getIsCompleted()))
                .count();
        double averageProgressPercent = progressList.stream()
                .mapToDouble(this::progressPercent)
                .average()
                .orElse(0.0);
        Long totalLearningSeconds = learningActivityDailyRepository.sumActiveSecondsByUserId(userId);
        return new UserLearningSummaryResponse(
                completedCourseCount,
                averageProgressPercent,
                totalLearningSeconds != null ? totalLearningSeconds : 0L
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressResponse> getUserCourseProgress(String userId, List<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return courseProgressRepository.findAllByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .map(courseProgressMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningActivityByMonthResponse> getUserActivityByMonth(String userId, Integer months) {
        int monthCount = months != null ? months : 6;
        if (monthCount <= 0) {
            throw new BadRequestException("Months must be greater than 0");
        }
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(monthCount - 1L);
        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDate = endMonth.atEndOfMonth();

        Map<String, Long> activityByMonth = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 0; i < monthCount; i++) {
            activityByMonth.put(startMonth.plusMonths(i).format(formatter), 0L);
        }
        learningActivityDailyRepository.aggregateUserActivityByMonth(userId, startDate, endDate)
                .forEach(row -> activityByMonth.put((String) row[0], ((Number) row[1]).longValue()));

        return activityByMonth.entrySet().stream()
                .map(entry -> new LearningActivityByMonthResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoursesProgressSummaryResponse getCoursesProgressSummary(List<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return new CoursesProgressSummaryResponse(0L, 0L, 0.0, List.of());
        }

        Map<String, List<CourseProgress>> progressByCourse = courseProgressRepository.findAllByCourseIdIn(courseIds)
                .stream()
                .collect(Collectors.groupingBy(CourseProgress::getCourseId));

        List<CourseProgressSummaryResponse> courses = courseIds.stream()
                .distinct()
                .map(courseId -> toCourseSummary(courseId, progressByCourse.getOrDefault(courseId, List.of())))
                .toList();

        long totalStudentProgressCount = courses.stream().mapToLong(CourseProgressSummaryResponse::studentCount).sum();
        long completedStudentProgressCount = courses.stream().mapToLong(CourseProgressSummaryResponse::completedStudentCount).sum();
        double completionRate = totalStudentProgressCount > 0
                ? completedStudentProgressCount * 100.0 / totalStudentProgressCount
                : 0.0;
        return new CoursesProgressSummaryResponse(totalStudentProgressCount, completedStudentProgressCount, completionRate, courses);
    }

    private CourseProgressSummaryResponse toCourseSummary(String courseId, List<CourseProgress> progressList) {
        long studentCount = progressList.size();
        long completedStudentCount = progressList.stream()
                .filter(progress -> Boolean.TRUE.equals(progress.getIsCompleted()))
                .count();
        double averageProgressPercent = progressList.stream()
                .mapToDouble(this::progressPercent)
                .average()
                .orElse(0.0);
        double completionRate = studentCount > 0 ? completedStudentCount * 100.0 / studentCount : 0.0;
        return new CourseProgressSummaryResponse(courseId, studentCount, completedStudentCount, averageProgressPercent, completionRate);
    }

    private double progressPercent(CourseProgress progress) {
        if (progress.getTotalItems() == null || progress.getTotalItems() <= 0) {
            return 0.0;
        }
        int completedItems = progress.getCompletedItems() != null ? progress.getCompletedItems() : 0;
        return completedItems * 100.0 / progress.getTotalItems();
    }
}
