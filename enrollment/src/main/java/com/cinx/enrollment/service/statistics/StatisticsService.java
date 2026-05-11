package com.cinx.enrollment.service.statistics;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.common.utils.AuthenticationUtil;
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
import java.time.temporal.ChronoUnit;
import com.cinx.common.exception.BadRequestException;
import java.util.ArrayList;
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

    @Value("${platform.fee.percentage:20}")
    private int platformFeePercentage;

    @Override
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

    @Override
    public InstructorStatisticsResponse getInstructorYearlyOverview(Integer year) {
        return buildInstructorStatistics(year, null, null, null);
    }

    @Override
    public InstructorStatisticsResponse getInstructorMonthlyOverview(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new BadRequestException("Year and month are required");
        }
        return buildInstructorStatistics(year, month, null, null);
    }

    @Override
    public InstructorStatisticsResponse getInstructorRangeOverview(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        return buildInstructorStatistics(null, null, startDate, endDate);
    }

    private InstructorStatisticsResponse buildInstructorStatistics(Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
        String instructorId = AuthenticationUtil.extractUserId();
        DateRangeResult range = resolveDateRange(year, month, startDate, endDate);

        Long totalGross = orderItemRepository.sumGrossRevenueByInstructor(instructorId, range.start, range.end);
        if (totalGross == null) totalGross = 0L;
        Long totalNet = calculateNetRevenue(totalGross);

        List<CourseStats> topCourses = orderItemRepository.findTopCoursesByRevenueForInstructor(
                instructorId, range.start, range.end, PageRequest.of(0, 5)).getContent();

        List<Object[]> timeStats = range.groupByDay ?
                orderItemRepository.aggregateRevenueByDayForInstructor(instructorId, range.start, range.end) :
                orderItemRepository.aggregateRevenueByMonthForInstructor(instructorId, range.start, range.end);

        List<RevenueByTimeResponse> revenueByTime = timeStats.stream().map(row -> {
            String timeLabel = (String) row[0];
            Long gross = ((Number) row[1]).longValue();
            return new RevenueByTimeResponse(timeLabel, gross, calculateNetRevenue(gross));
        }).collect(Collectors.toList());

        return new InstructorStatisticsResponse(totalGross, totalNet, revenueByTime, topCourses);
    }

    @Override
    public CourseStatisticsResponse getCourseStatistics(String courseId, Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
        String instructorId = AuthenticationUtil.extractUserId();
        DateRangeResult range = resolveDateRange(year, month, startDate, endDate);

        Long totalGross = orderItemRepository.sumGrossRevenueByCourseId(instructorId, courseId, range.start, range.end);
        if (totalGross == null) totalGross = 0L;
        Long totalNet = calculateNetRevenue(totalGross);

        List<Object[]> timeStats = range.groupByDay ?
                orderItemRepository.aggregateRevenueByDayForCourse(instructorId, courseId, range.start, range.end) :
                orderItemRepository.aggregateRevenueByMonthForCourse(instructorId, courseId, range.start, range.end);

        List<RevenueByTimeResponse> revenueByTime = timeStats.stream().map(row -> {
            String timeLabel = (String) row[0];
            Long gross = ((Number) row[1]).longValue();
            return new RevenueByTimeResponse(timeLabel, gross, calculateNetRevenue(gross));
        }).collect(Collectors.toList());

        return new CourseStatisticsResponse(totalGross, totalNet, revenueByTime);
    }

    @Override
    public AdminOverviewResponse getAdminYearlyOverview(Integer year) {
        return buildAdminStatistics(year, null, null, null);
    }

    @Override
    public AdminOverviewResponse getAdminMonthlyOverview(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new BadRequestException("Year and month are required");
        }
        return buildAdminStatistics(year, month, null, null);
    }

    @Override
    public AdminOverviewResponse getAdminRangeOverview(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        return buildAdminStatistics(null, null, startDate, endDate);
    }

    private AdminOverviewResponse buildAdminStatistics(Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
        DateRangeResult range = resolveDateRange(year, month, startDate, endDate);

        Long totalGross = orderRepository.sumRevenueBetween(OrderStatus.PAID, range.start, range.end);
        if (totalGross == null) totalGross = 0L;
        
        Long totalPlatformFee = totalGross - calculateNetRevenue(totalGross);
        Long totalOrders = orderRepository.countOrdersBetween(OrderStatus.PAID, range.start, range.end);

        List<CourseStats> topCourses = orderItemRepository.findTopCoursesByRevenue(
                range.start, range.end, PageRequest.of(0, 5)).getContent();

        List<Object[]> timeStats = range.groupByDay ?
                orderItemRepository.aggregatePlatformRevenueByDay(range.start, range.end) :
                orderItemRepository.aggregatePlatformRevenueByMonth(range.start, range.end);

        List<RevenueByTimeResponse> revenueByTime = timeStats.stream().map(row -> {
            String timeLabel = (String) row[0];
            Long gross = ((Number) row[1]).longValue();
            Long platformFee = gross - calculateNetRevenue(gross); // the net here is the platform's net, which is the fee taken
            // But we represent 'netRevenue' for the platform as the platform fee. Or keep gross as gross and net as platform fee.
            return new RevenueByTimeResponse(timeLabel, gross, platformFee);
        }).collect(Collectors.toList());

        return new AdminOverviewResponse(totalGross, totalPlatformFee, totalOrders, revenueByTime, topCourses);
    }

    private Long calculateNetRevenue(Long grossRevenue) {
        return grossRevenue * (100 - platformFeePercentage) / 100;
    }

    private DateRangeResult resolveDateRange(Integer year, Integer month, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start;
        LocalDateTime end;
        boolean groupByDay;

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new BadRequestException("Start date cannot be after end date");
            }
            if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
                throw new BadRequestException("Date range cannot exceed 31 days");
            }
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59, 999999999);
            groupByDay = true;
        } else if (year != null) {
            if (month != null) {
                YearMonth targetMonth = YearMonth.of(year, month);
                start = targetMonth.atDay(1).atStartOfDay();
                end = targetMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);
                groupByDay = true;
            } else {
                start = LocalDateTime.of(year, 1, 1, 0, 0);
                end = LocalDateTime.of(year, 12, 31, 23, 59, 59, 999999999);
                groupByDay = false; // Group by month for entire year overview
            }
        } else {
            // Default to all time
            start = LocalDateTime.of(1970, 1, 1, 0, 0);
            end = LocalDateTime.of(2099, 12, 31, 23, 59, 59, 999999999);
            groupByDay = false;
        }
        return new DateRangeResult(start, end, groupByDay);
    }

    private record DateRangeResult(LocalDateTime start, LocalDateTime end, boolean groupByDay) {}
}
