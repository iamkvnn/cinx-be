package com.cinx.enrollment.service.statistics;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import com.cinx.enrollment.dto.response.CourseStats;
import com.cinx.enrollment.dto.response.DashboardMetricsResponse;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.course.CourseService;
import com.cinx.enrollment.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final EnrolledCourseRepository enrolledCourseRepository;
    private final UserService userService;
    private final CourseService courseService;

    public DashboardMetricsResponse getDashboardMetrics(Integer year, Integer month) {
        YearMonth targetMonth = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        Long totalRevenue = orderRepository.sumRevenueBetween(OrderStatus.PAID, startOfMonth, endOfMonth);
        Long paidOrdersThisMonth = orderRepository.countOrdersBetween(OrderStatus.PAID, startOfMonth, endOfMonth);

        Long totalUsers = 0L;
        Long newUsersThisMonth = 0L;
        try {
            totalUsers = userService.getTotalUsersCount().data();
            newUsersThisMonth = userService.getNewUsersCount(startOfMonth.format(DateTimeFormatter.ISO_DATE_TIME), endOfMonth.format(DateTimeFormatter.ISO_DATE_TIME)).data();
        } catch (Exception e) {
            // Fallback or log if user service is unavailable
        }

        List<Object[]> topCoursesData = enrolledCourseRepository.findTopEnrolledCourses(startOfMonth, endOfMonth, PageRequest.of(0, 5));
        
        List<String> topCourseIds = topCoursesData.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());

        Map<String, String> courseTitles = Map.of();
        if (!topCourseIds.isEmpty()) {
            try {
                List<CourseResponse> courses = courseService.getCoursesByIds(topCourseIds).data();
                courseTitles = courses.stream().collect(Collectors.toMap(CourseResponse::id, CourseResponse::title));
            } catch (Exception e) {
                // Ignore course service failure
            }
        }

        Map<String, String> finalCourseTitles = courseTitles;
        List<CourseStats> topEnrolledCourses = topCoursesData.stream()
                .map(row -> new CourseStats(
                        (String) row[0],
                        finalCourseTitles.getOrDefault((String) row[0], "Unknown Course"),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());

        return new DashboardMetricsResponse(
                totalRevenue != null ? totalRevenue : 0L,
                totalUsers != null ? totalUsers : 0L,
                newUsersThisMonth != null ? newUsersThisMonth : 0L,
                topEnrolledCourses,
                paidOrdersThisMonth != null ? paidOrdersThisMonth : 0L
        );
    }
}
