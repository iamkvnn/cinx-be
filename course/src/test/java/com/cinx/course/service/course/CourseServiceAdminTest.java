package com.cinx.course.service.course;

import com.cinx.course.dto.response.InstructorCourseSummaryResponse;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.RejectCourseReasonRepository;
import com.cinx.course.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceAdminTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private RejectCourseReasonRepository rejectCourseReasonRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private UserService userService;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CourseEventProducer courseEventProducer;
    @InjectMocks
    private CourseService courseService;

    @Test
    void getInstructorCourseSummaryReturnsCountsAndAverageRating() {
        when(courseRepository.countByInstructorId("inst-1")).thenReturn(8L);
        when(courseRepository.countByInstructorIdAndIsPublishedTrue("inst-1")).thenReturn(6L);
        when(courseRepository.averageRatingByInstructorId("inst-1")).thenReturn(4.75);

        InstructorCourseSummaryResponse summary = courseService.getInstructorCourseSummary("inst-1");

        assertThat(summary.courseCount()).isEqualTo(8L);
        assertThat(summary.publishedCourseCount()).isEqualTo(6L);
        assertThat(summary.averageRating()).isEqualTo(4.75);
    }
}
