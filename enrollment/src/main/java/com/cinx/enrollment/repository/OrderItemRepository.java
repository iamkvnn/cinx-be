package com.cinx.enrollment.repository;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.time.LocalDateTime;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrderId(String orderId);

    @Query("SELECT oi FROM OrderItem oi JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId")
    Page<OrderItem> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT oi FROM OrderItem oi JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId AND oi.courseId IN :courseIds")
    List<OrderItem> findAllByCourseIdsAndUserId(List<String> courseIds, String userId);

    @Query("SELECT oi.courseId, COALESCE(SUM(oi.discountedPrice), 0), MAX(o.orderDate) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = :status AND o.userId = :userId AND oi.courseId IN :courseIds " +
           "GROUP BY oi.courseId")
    List<Object[]> aggregatePaidAmountByUserAndCourseIds(OrderStatus status, String userId, List<String> courseIds);

    @Query("SELECT SUM(oi.discountedPrice) FROM OrderItem oi JOIN Order o ON oi.orderId = o.id WHERE o.status = 1 AND oi.instructorId = :instructorId AND o.orderDate BETWEEN :startDate AND :endDate")
    Long sumGrossRevenueByInstructor(String instructorId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT oi.courseId, oi.title, COUNT(oi.id), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND oi.instructorId = :instructorId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.courseId, oi.title " +
           "ORDER BY SUM(oi.discountedPrice) DESC")
    List<Object[]> aggregateRevenueByCourseForInstructor(String instructorId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d'), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND oi.instructorId = :instructorId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') ASC")
    List<Object[]> aggregateRevenueByDayForInstructor(String instructorId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m'), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND oi.instructorId = :instructorId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') ASC")
    List<Object[]> aggregateRevenueByMonthForInstructor(String instructorId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT oi.courseId, oi.title, COUNT(oi.id), COALESCE(SUM(oi.discountedPrice), 0) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = :status AND oi.instructorId = :instructorId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.courseId, oi.title " +
           "ORDER BY SUM(oi.discountedPrice) DESC")
    List<Object[]> aggregateCourseRevenueByInstructor(OrderStatus status, String instructorId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(oi.discountedPrice) FROM OrderItem oi JOIN Order o ON oi.orderId = o.id WHERE o.status = 1 AND oi.instructorId = :instructorId AND oi.courseId = :courseId AND o.orderDate BETWEEN :startDate AND :endDate")
    Long sumGrossRevenueByCourseId(String instructorId, String courseId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d'), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND oi.instructorId = :instructorId AND oi.courseId = :courseId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') ASC")
    List<Object[]> aggregateRevenueByDayForCourse(String instructorId, String courseId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m'), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND oi.instructorId = :instructorId AND oi.courseId = :courseId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') ASC")
    List<Object[]> aggregateRevenueByMonthForCourse(String instructorId, String courseId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d'), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') ASC")
    List<Object[]> aggregatePlatformRevenueByDay(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m'), SUM(oi.discountedPrice) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m') ASC")
    List<Object[]> aggregatePlatformRevenueByMonth(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT new com.cinx.enrollment.dto.response.CourseRevenueStats(oi.courseId, oi.title, SUM(oi.discountedPrice)) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND oi.instructorId = :instructorId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.courseId, oi.title " +
           "ORDER BY SUM(oi.discountedPrice) DESC")
    Page<com.cinx.enrollment.dto.response.CourseRevenueStats> findTopCoursesByRevenueForInstructor(String instructorId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT new com.cinx.enrollment.dto.response.CourseRevenueStats(oi.courseId, oi.title, SUM(oi.discountedPrice)) " +
           "FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.status = 1 AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.courseId, oi.title " +
           "ORDER BY SUM(oi.discountedPrice) DESC")
    Page<com.cinx.enrollment.dto.response.CourseRevenueStats> findTopCoursesByRevenue(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
