package com.cinx.enrollment.service.enrollment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import com.cinx.enrollment.dto.response.EnrolledCourseResponse;
import com.cinx.enrollment.dto.response.UserEnrollmentSummaryResponse;
import com.cinx.enrollment.messaging.EnrolledCourseEventProducer;
import com.cinx.enrollment.model.EnrolledCourse;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.course.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private EnrolledCourseEventProducer enrolledCourseEventProducer;
    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void getEnrolledCoursesQueriesCourseServiceAndMapsEnrolledAtSortAlias() {
        EnrolledCourse first = enrolledCourse("course-1", "2025-01-01T10:00:00");
        EnrolledCourse second = enrolledCourse("course-2", "2025-01-02T10:00:00");
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(enrolledCourseRepository.findAllByUserId(eq("user-1"), argThat(Pageable::isUnpaged)))
                .thenReturn(new PageImpl<>(List.of(first, second)));
        when(courseService.searchCourseIds(List.of("course-1", "course-2"), "java"))
                .thenReturn(new ApiResponse<>(true, "ok", List.of("course-2")));
        when(enrolledCourseRepository.findAllByUserIdAndCourseIdIn(eq("user-1"), eq(List.of("course-2")), pageableCaptor.capture()))
                .thenReturn(new PageImpl<>(List.of(second), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1));
        when(courseService.getCoursesByIds(List.of("course-2")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(course("course-2"))));

        Page<EnrolledCourseResponse> result = enrollmentService.getEnrolledCourses(
                "user-1",
                1,
                10,
                " java ",
                "{\"enrolledAt\":\"DESC\"}");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().course().id()).isEqualTo("course-2");
        assertThat(result.getContent().getFirst().enrolledAt()).isEqualTo(LocalDateTime.parse("2025-01-02T10:00:00"));
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getUserEnrollmentSummaryUsesPaidOrdersAndEnrollments() {
        when(enrolledCourseRepository.countByUserId("user-1")).thenReturn(12L);
        when(orderRepository.sumRevenueByUserId(OrderStatus.PAID, "user-1")).thenReturn(4_500_000L);
        when(orderRepository.countOrdersByUserId(OrderStatus.PAID, "user-1")).thenReturn(5L);

        UserEnrollmentSummaryResponse summary = enrollmentService.getUserEnrollmentSummary("user-1");

        assertThat(summary.enrolledCourseCount()).isEqualTo(12L);
        assertThat(summary.totalSpent()).isEqualTo(4_500_000L);
        assertThat(summary.paidOrderCount()).isEqualTo(5L);
    }

    private EnrolledCourse enrolledCourse(String courseId, String createdAt) {
        EnrolledCourse enrolledCourse = EnrolledCourse.builder()
                .courseId(courseId)
                .userId("user-1")
                .build();
        enrolledCourse.setCreatedAt(LocalDateTime.parse(createdAt));
        return enrolledCourse;
    }

    private CourseResponse course(String id) {
        return new CourseResponse(
                id,
                "Java Programming",
                "Course description",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "PUBLISHED",
                "PUBLISHED",
                null,
                null);
    }
}
