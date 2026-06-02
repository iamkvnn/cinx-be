package com.cinx.enrollment.service.statistics;

import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.response.AdminOverviewResponse;
import com.cinx.enrollment.dto.response.CourseStatisticsResponse;
import com.cinx.enrollment.dto.response.InstructorStatisticsResponse;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.course.CourseService;
import com.cinx.enrollment.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;
    @Mock
    private UserService userService;
    @Mock
    private CourseService courseService;
    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(statisticsService, "platformFeePercentage", 20);
    }

    @Test
    void instructorOverviewZeroFillsMonthlySeries() {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(2);
        String middleMonth = startMonth.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        when(orderItemRepository.sumGrossRevenueByInstructor(eq("instructor-1"), any(), any())).thenReturn(500L);
        when(orderItemRepository.findTopCoursesByRevenueForInstructor(eq("instructor-1"), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(orderItemRepository.aggregateRevenueByMonthForInstructor(eq("instructor-1"), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{middleMonth, 500L}));

        InstructorStatisticsResponse response;
        try (MockedStatic<AuthenticationUtil> authentication = Mockito.mockStatic(AuthenticationUtil.class)) {
            authentication.when(AuthenticationUtil::extractUserId).thenReturn("instructor-1");
            response = statisticsService.getInstructorOverview(
                    StatisticsGroupBy.MONTH,
                    startMonth.atDay(1),
                    endMonth.atEndOfMonth()
            );
        }

        assertThat(response.revenueByTime()).hasSize(3);
        assertThat(response.revenueByTime().get(0).grossRevenue()).isZero();
        assertThat(response.revenueByTime().get(1).timeLabel()).isEqualTo(middleMonth);
        assertThat(response.revenueByTime().get(1).grossRevenue()).isEqualTo(500L);
        assertThat(response.revenueByTime().get(1).netRevenue()).isEqualTo(400L);
    }

    @Test
    void courseStatisticsZeroFillsDailySeries() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(2);
        String middleDay = startDate.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        when(orderItemRepository.sumGrossRevenueByCourseId(eq("instructor-1"), eq("course-1"), any(), any()))
                .thenReturn(600L);
        when(orderItemRepository.aggregateRevenueByDayForCourse(eq("instructor-1"), eq("course-1"), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{middleDay, 600L}));

        CourseStatisticsResponse response;
        try (MockedStatic<AuthenticationUtil> authentication = Mockito.mockStatic(AuthenticationUtil.class)) {
            authentication.when(AuthenticationUtil::extractUserId).thenReturn("instructor-1");
            response = statisticsService.getCourseStatistics(
                    "course-1",
                    StatisticsGroupBy.DAY,
                    startDate,
                    endDate
            );
        }

        assertThat(response.revenueByTime()).hasSize(3);
        assertThat(response.revenueByTime().get(0).grossRevenue()).isZero();
        assertThat(response.revenueByTime().get(1).timeLabel()).isEqualTo(middleDay);
        assertThat(response.revenueByTime().get(1).netRevenue()).isEqualTo(480L);
    }

    @Test
    void adminOverviewZeroFillsMonthlySeries() {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(1);
        String endLabel = endMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        when(orderRepository.sumRevenueBetween(eq(OrderStatus.PAID), any(), any())).thenReturn(1_000L);
        when(orderRepository.countOrdersBetween(eq(OrderStatus.PAID), any(), any())).thenReturn(2L);
        when(orderItemRepository.findTopCoursesByRevenue(any(), any(), any(Pageable.class))).thenReturn(Page.empty());
        when(orderItemRepository.aggregatePlatformRevenueByMonth(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{endLabel, 1_000L}));

        AdminOverviewResponse response = statisticsService.getAdminOverview(
                StatisticsGroupBy.MONTH,
                startMonth.atDay(1),
                endMonth.atEndOfMonth()
        );

        assertThat(response.platformRevenueByTime()).hasSize(2);
        assertThat(response.platformRevenueByTime().get(0).grossRevenue()).isZero();
        assertThat(response.platformRevenueByTime().get(1).netRevenue()).isEqualTo(200L);
    }

}
