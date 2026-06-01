package com.cinx.enrollment.service.enrollment;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.response.UserEnrollmentSummaryResponse;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private EnrollmentService enrollmentService;

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
}
