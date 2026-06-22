package com.cinx.course.service.statistics;

import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.response.InstructorCourseStatisticsOverviewResponse;
import com.cinx.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseStatisticsServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @InjectMocks
    private CourseStatisticsService courseStatisticsService;

    @Test
    void instructorOverviewKeepsAverageRatingNullWhenUnrated() {
        when(courseRepository.aggregateCreatedCoursesByInstructorAndDay(eq("inst-1"), any(), any()))
                .thenReturn(List.of());
        when(courseRepository.countCreatedCoursesByInstructorBetween(eq("inst-1"), any(), any()))
                .thenReturn(0L);
        when(courseRepository.countByInstructorId("inst-1")).thenReturn(2L);
        when(courseRepository.countByInstructorIdAndStatus("inst-1", CourseStatus.PUBLISHED)).thenReturn(1L);
        when(courseRepository.averageRatingByInstructorId("inst-1")).thenReturn(null);
        when(courseRepository.sumEnrollmentCountByInstructorId("inst-1")).thenReturn(0L);

        InstructorCourseStatisticsOverviewResponse response = courseStatisticsService.getInstructorOverview(
                "inst-1",
                StatisticsGroupBy.DAY,
                LocalDate.now().minusDays(1),
                LocalDate.now()
        );

        assertThat(response.averageRating()).isNull();
        assertThat(response.currentCourseCount()).isEqualTo(2L);
        assertThat(response.currentPublishedCourseCount()).isEqualTo(1L);
    }
}
