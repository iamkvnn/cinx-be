package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrderId(String orderId);

    @Query("SELECT oi FROM OrderItem oi JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId")
    Page<OrderItem> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT oi FROM OrderItem oi JOIN Order o ON oi.orderId = o.id WHERE o.userId = :userId AND oi.courseId IN :courseIds")
    List<OrderItem> findAllByCourseIdsAndUserId(List<String> courseIds, String userId);
}
