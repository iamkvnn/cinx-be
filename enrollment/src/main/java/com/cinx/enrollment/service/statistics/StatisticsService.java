package com.cinx.enrollment.service.statistics;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.response.*;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.course.CourseService;
import com.cinx.enrollment.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
public class StatisticsService implements IStatisticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EnrolledCourseRepository enrolledCourseRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final StatisticsRangeResolver statisticsRangeResolver = new StatisticsRangeResolver();

    @Value("${platform.fee.percentage:20}")
    private int platformFeePercentage;

    @Override
    public DashboardMetricsResponse getDashboardMetrics(Integer year, Integer month) {
        YearMonth targetMonth = statisticsRangeResolver.resolveDashboardMonth(year, month);
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

    @Override
    public InstructorStatisticsResponse getInstructorOverview(String instructorId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        return buildInstructorStatistics(instructorId, statisticsRangeResolver.resolve(groupBy, startDate, endDate));
    }

    private InstructorStatisticsResponse buildInstructorStatistics(String instructorId, StatisticsDateRange range) {
        Long totalGross = orderItemRepository.sumGrossRevenueByInstructor(instructorId, range.start(), range.end());
        if (totalGross == null) totalGross = 0L;
        Long totalNet = calculateNetRevenue(totalGross);

        List<CourseStats> topCoursesByRevenue = orderItemRepository.findTopCoursesByRevenueForInstructor(
                instructorId, range.start(), range.end(), PageRequest.of(0, 5)).getContent();
        List<String> courseIds = orderItemRepository.aggregateCourseRevenueByInstructor(OrderStatus.PAID, instructorId, range.start(), range.end())
                .stream()
                .map(row -> (String) row[0])
                .toList();
        Long enrollmentsInRange = courseIds.isEmpty() ? 0L : enrolledCourseRepository.countEnrollmentsByCourseIdsBetween(courseIds, range.start(), range.end());
        Long distinctLearnersInRange = courseIds.isEmpty() ? 0L : enrolledCourseRepository.countDistinctLearnersByCourseIdsBetween(courseIds, range.start(), range.end());
        List<CourseStats> topCoursesByEnrollment = courseIds.isEmpty()
                ? List.of()
                : toCourseStats(enrolledCourseRepository.findTopEnrolledCoursesByCourseIds(courseIds, range.start(), range.end(), PageRequest.of(0, 5)));

        List<Object[]> timeStats = range.groupByDay() ?
                orderItemRepository.aggregateRevenueByDayForInstructor(instructorId, range.start(), range.end()) :
                orderItemRepository.aggregateRevenueByMonthForInstructor(instructorId, range.start(), range.end());

        List<RevenueByTimeResponse> revenueByTime = fillRevenueByTime(range, timeStats, false);
        List<Object[]> enrollmentRows = courseIds.isEmpty()
                ? List.of()
                : range.groupByDay()
                        ? enrolledCourseRepository.aggregateEnrollmentsByCourseIdsAndDay(courseIds, range.start(), range.end())
                        : enrolledCourseRepository.aggregateEnrollmentsByCourseIdsAndMonth(courseIds, range.start(), range.end());
        List<EnrollmentByTimeResponse> enrollmentsByTime = fillEnrollmentsByTime(range, enrollmentRows);

        return new InstructorStatisticsResponse(
                totalGross,
                totalNet,
                enrollmentsInRange,
                distinctLearnersInRange,
                revenueByTime,
                enrollmentsByTime,
                topCoursesByRevenue,
                topCoursesByEnrollment
        );
    }

    @Override
    public CourseStatisticsResponse getCourseStatistics(String instructorId, String courseId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsDateRange range = statisticsRangeResolver.resolve(groupBy, startDate, endDate);
        return buildCourseStatistics(instructorId, courseId, range);
    }

    private CourseStatisticsResponse buildCourseStatistics(String instructorId, String courseId, StatisticsDateRange range) {
        Long totalGross = orderItemRepository.sumGrossRevenueByCourseId(instructorId, courseId, range.start(), range.end());
        if (totalGross == null) totalGross = 0L;
        Long totalNet = calculateNetRevenue(totalGross);

        List<Object[]> timeStats = range.groupByDay() ?
                orderItemRepository.aggregateRevenueByDayForCourse(instructorId, courseId, range.start(), range.end()) :
                orderItemRepository.aggregateRevenueByMonthForCourse(instructorId, courseId, range.start(), range.end());

        List<RevenueByTimeResponse> revenueByTime = fillRevenueByTime(range, timeStats, false);

        return new CourseStatisticsResponse(totalGross, totalNet, revenueByTime);
    }

    @Override
    public InstructorRevenueResponse getInstructorRevenueSeries(String instructorId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsDateRange range = statisticsRangeResolver.resolve(groupBy, startDate, endDate);
        return buildInstructorRevenue(instructorId, range);
    }

    private InstructorRevenueResponse buildInstructorRevenue(String instructorId, StatisticsDateRange range) {
        Long totalRevenue = orderItemRepository.sumGrossRevenueByInstructor(instructorId, range.start(), range.end());
        if (totalRevenue == null) totalRevenue = 0L;

        List<Object[]> timeStats = range.groupByDay()
                ? orderItemRepository.aggregateRevenueByDayForInstructor(instructorId, range.start(), range.end())
                : orderItemRepository.aggregateRevenueByMonthForInstructor(instructorId, range.start(), range.end());
        List<RevenueByTimeResponse> revenueByTime = fillRevenueByTime(range, timeStats, false);

        List<CourseRevenueResponse> courseRevenues = orderItemRepository
                .aggregateCourseRevenueByInstructor(OrderStatus.PAID, instructorId, range.start(), range.end())
                .stream()
                .map(row -> new CourseRevenueResponse(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .toList();

        return new InstructorRevenueResponse(totalRevenue, revenueByTime, courseRevenues);
    }

    @Override
    public AdminOverviewResponse getAdminOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        return buildAdminStatistics(statisticsRangeResolver.resolve(groupBy, startDate, endDate));
    }

    private AdminOverviewResponse buildAdminStatistics(StatisticsDateRange range) {
        Long totalGross = orderRepository.sumRevenueBetween(OrderStatus.PAID, range.start(), range.end());
        if (totalGross == null) totalGross = 0L;
        
        Long totalPlatformFee = totalGross - calculateNetRevenue(totalGross);
        Long totalOrders = orderRepository.countOrdersBetween(OrderStatus.PAID, range.start(), range.end());
        Long enrollmentsInRange = enrolledCourseRepository.countEnrollmentsBetween(range.start(), range.end());
        Long distinctLearnersInRange = enrolledCourseRepository.countDistinctLearnersBetween(range.start(), range.end());

        List<CourseStats> topCoursesByRevenue = orderItemRepository.findTopCoursesByRevenue(
                range.start(), range.end(), PageRequest.of(0, 5)).getContent();
        List<CourseStats> topCoursesByEnrollment = toCourseStats(
                enrolledCourseRepository.findTopEnrolledCourses(range.start(), range.end(), PageRequest.of(0, 5)));

        List<Object[]> timeStats = range.groupByDay() ?
                orderItemRepository.aggregatePlatformRevenueByDay(range.start(), range.end()) :
                orderItemRepository.aggregatePlatformRevenueByMonth(range.start(), range.end());

        List<RevenueByTimeResponse> revenueByTime = fillRevenueByTime(range, timeStats, true);
        List<Object[]> enrollmentRows = range.groupByDay()
                ? enrolledCourseRepository.aggregateEnrollmentsByDay(range.start(), range.end())
                : enrolledCourseRepository.aggregateEnrollmentsByMonth(range.start(), range.end());
        List<EnrollmentByTimeResponse> enrollmentsByTime = fillEnrollmentsByTime(range, enrollmentRows);

        return new AdminOverviewResponse(
                totalGross,
                totalPlatformFee,
                totalOrders,
                enrollmentsInRange,
                totalOrders,
                distinctLearnersInRange,
                revenueByTime,
                enrollmentsByTime,
                topCoursesByRevenue,
                topCoursesByEnrollment
        );
    }

    private Long calculateNetRevenue(Long grossRevenue) {
        return grossRevenue * (100 - platformFeePercentage) / 100;
    }

    private List<RevenueByTimeResponse> fillRevenueByTime(StatisticsDateRange range, List<Object[]> rows, boolean platformRevenue) {
        Map<String, Long> revenueByLabel = new LinkedHashMap<>();
        range.bucketLabels().forEach(label -> revenueByLabel.put(label, 0L));
        rows.forEach(row -> revenueByLabel.put((String) row[0], ((Number) row[1]).longValue()));

        return revenueByLabel.entrySet().stream()
                .map(entry -> {
                    Long gross = entry.getValue();
                    Long net = platformRevenue ? gross - calculateNetRevenue(gross) : calculateNetRevenue(gross);
                    return new RevenueByTimeResponse(entry.getKey(), gross, net);
                })
                .toList();
    }

    private List<EnrollmentByTimeResponse> fillEnrollmentsByTime(StatisticsDateRange range, List<Object[]> rows) {
        Map<String, Long> enrollmentsByLabel = new LinkedHashMap<>();
        range.bucketLabels().forEach(label -> enrollmentsByLabel.put(label, 0L));
        rows.forEach(row -> enrollmentsByLabel.put((String) row[0], ((Number) row[1]).longValue()));
        return enrollmentsByLabel.entrySet().stream()
                .map(entry -> new EnrollmentByTimeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<CourseStats> toCourseStats(List<Object[]> rows) {
        Map<String, String> courseTitles = getCourseTitles(rows.stream()
                .map(row -> (String) row[0])
                .toList());
        return rows.stream()
                .map(row -> {
                    String courseId = (String) row[0];
                    return new CourseStats(
                            courseId,
                            courseTitles.getOrDefault(courseId, "Unknown Course"),
                            ((Number) row[1]).longValue()
                    );
                })
                .toList();
    }

    private Map<String, String> getCourseTitles(List<String> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        try {
            return courseService.getCoursesByIds(courseIds).data().stream()
                    .collect(Collectors.toMap(CourseResponse::id, CourseResponse::title, (existing, replacement) -> existing));
        } catch (Exception e) {
            return Map.of();
        }
    }
}
