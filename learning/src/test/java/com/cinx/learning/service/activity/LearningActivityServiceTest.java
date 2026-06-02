package com.cinx.learning.service.activity;

import com.cinx.learning.dto.request.LearningActivityRequest;
import com.cinx.learning.dto.response.CoursesProgressSummaryResponse;
import com.cinx.learning.dto.response.LearningActivityByTimeResponse;
import com.cinx.learning.dto.response.UserLearningSummaryResponse;
import com.cinx.learning.mapper.CourseProgressMapper;
import com.cinx.learning.model.CourseProgress;
import com.cinx.learning.model.LearningActivityDaily;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningActivityDailyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningActivityServiceTest {
    @Mock
    private LearningActivityDailyRepository learningActivityDailyRepository;
    @Mock
    private CourseProgressRepository courseProgressRepository;
    @Mock
    private CourseProgressMapper courseProgressMapper;
    @InjectMocks
    private LearningActivityService learningActivityService;

    @Test
    void recordActivityCapsHeartbeatAtFiveMinutes() {
        when(learningActivityDailyRepository.findByUserIdAndCourseIdAndActivityDate("user-1", "course-1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(learningActivityDailyRepository.save(any(LearningActivityDaily.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        learningActivityService.recordActivity("user-1", new LearningActivityRequest("course-1", "lesson-1", 1_000));

        ArgumentCaptor<LearningActivityDaily> captor = ArgumentCaptor.forClass(LearningActivityDaily.class);
        verify(learningActivityDailyRepository).save(captor.capture());
        assertThat(captor.getValue().getActiveSeconds()).isEqualTo(300L);
        assertThat(captor.getValue().getLastActivityAt()).isNotNull();
    }

    @Test
    void getUserLearningSummaryAggregatesProgressAndTime() {
        CourseProgress first = progress("course-1", 10, 10, true);
        CourseProgress second = progress("course-2", 5, 10, false);
        when(courseProgressRepository.findAllByUserId("user-1")).thenReturn(List.of(first, second));
        when(learningActivityDailyRepository.sumActiveSecondsByUserId("user-1")).thenReturn(12_600L);

        UserLearningSummaryResponse summary = learningActivityService.getUserLearningSummary("user-1");

        assertThat(summary.completedCourseCount()).isEqualTo(1L);
        assertThat(summary.averageProgressPercent()).isEqualTo(75.0);
        assertThat(summary.totalLearningSeconds()).isEqualTo(12_600L);
    }

    @Test
    void getUserActivitySeriesFillsMissingMonthsWithZero() {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(2);
        String middleMonth = startMonth.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        when(learningActivityDailyRepository.aggregateUserActivityByMonth(
                "user-1",
                startMonth.atDay(1),
                endMonth.atEndOfMonth()
        )).thenReturn(List.<Object[]>of(new Object[]{middleMonth, 3600L}));

        List<LearningActivityByTimeResponse> activity = learningActivityService.getUserActivitySeries(
                "user-1",
                LearningActivityGroupBy.MONTH,
                startMonth.atDay(1),
                endMonth.atEndOfMonth()
        );

        assertThat(activity).hasSize(3);
        assertThat(activity.get(0).activeSeconds()).isZero();
        assertThat(activity.get(1).timeLabel()).isEqualTo(middleMonth);
        assertThat(activity.get(1).activeSeconds()).isEqualTo(3600L);
        assertThat(activity.get(2).activeSeconds()).isZero();
    }

    @Test
    void getUserActivitySeriesRejectsMoreThanTwelveMonths() {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(12);

        assertThatThrownBy(() -> learningActivityService.getUserActivitySeries(
                "user-1",
                LearningActivityGroupBy.MONTH,
                startMonth.atDay(1),
                endMonth.atEndOfMonth()
        ))
                .isInstanceOf(com.cinx.common.exception.BadRequestException.class);
    }

    @Test
    void getUserActivitySeriesFillsMissingDaysWithZero() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(2);
        String middleDay = startDate.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(learningActivityDailyRepository.aggregateUserActivityByDay("user-1", startDate, endDate))
                .thenReturn(List.<Object[]>of(new Object[]{middleDay, 900L}));

        List<LearningActivityByTimeResponse> activity = learningActivityService.getUserActivitySeries(
                "user-1",
                LearningActivityGroupBy.DAY,
                startDate,
                endDate
        );

        assertThat(activity).hasSize(3);
        assertThat(activity.get(0).activeSeconds()).isZero();
        assertThat(activity.get(1).timeLabel()).isEqualTo(middleDay);
        assertThat(activity.get(1).activeSeconds()).isEqualTo(900L);
        assertThat(activity.get(2).activeSeconds()).isZero();
    }

    @Test
    void getCoursesProgressSummaryAggregatesPerCourseAndTotals() {
        CourseProgress first = progress("course-1", 10, 10, true);
        CourseProgress second = progress("course-1", 5, 10, false);
        CourseProgress third = progress("course-2", 8, 10, false);
        when(courseProgressRepository.findAllByCourseIdIn(List.of("course-1", "course-2")))
                .thenReturn(List.of(first, second, third));

        CoursesProgressSummaryResponse summary = learningActivityService.getCoursesProgressSummary(List.of("course-1", "course-2"));

        assertThat(summary.totalStudentProgressCount()).isEqualTo(3L);
        assertThat(summary.completedStudentProgressCount()).isEqualTo(1L);
        assertThat(summary.courses()).hasSize(2);
        assertThat(summary.courses().get(0).averageProgressPercent()).isEqualTo(75.0);
    }

    private CourseProgress progress(String courseId, int completedItems, int totalItems, boolean completed) {
        CourseProgress progress = new CourseProgress();
        progress.setCourseId(courseId);
        progress.setCompletedItems(completedItems);
        progress.setTotalItems(totalItems);
        progress.setIsCompleted(completed);
        return progress;
    }
}
