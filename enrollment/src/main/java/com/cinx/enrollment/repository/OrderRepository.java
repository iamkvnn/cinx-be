package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByUserId(String userId, Pageable Pageable);

    @Query("""
           SELECT o FROM Order o
           WHERE o.userId = :userId AND
           (:query IS NULL or o.id LIKE %:query%)
           """)
    Page<Order> findAllByUserIdAndQuery(String userId, String query, Pageable Pageable);
}
