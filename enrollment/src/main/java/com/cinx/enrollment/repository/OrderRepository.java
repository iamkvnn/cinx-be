package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findAllByUserId(String userId, Pageable pageable);
}
