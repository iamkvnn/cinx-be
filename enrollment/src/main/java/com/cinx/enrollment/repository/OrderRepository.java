package com.cinx.enrollment.repository;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByUserId(String userId, Pageable Pageable);

    @Query("""
           SELECT o FROM Order o
           WHERE o.userId = :userId AND
           (:query IS NULL or o.id LIKE %:query%)
           """)
    Page<Order> findAllByUserIdAndQuery(String userId, String query, Pageable Pageable);

    @Query("SELECT SUM(o.totalPrice - o.discounted) FROM Order o WHERE o.status = :status")
    Long sumTotalRevenue(OrderStatus status);

    @Query("SELECT SUM(o.totalPrice - o.discounted) FROM Order o WHERE o.status = :status AND o.orderDate BETWEEN :start AND :end")
    Long sumRevenueBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.orderDate BETWEEN :start AND :end")
    Long countOrdersBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalPrice - o.discounted), 0) FROM Order o WHERE o.status = :status AND o.userId = :userId")
    Long sumRevenueByUserId(OrderStatus status, String userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.userId = :userId")
    Long countOrdersByUserId(OrderStatus status, String userId);
}
