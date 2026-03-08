package com.cinx.payment.repository;

import com.cinx.payment.model.MomoPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface MomoPaymentRepository extends JpaRepository<MomoPayment, String> {
    @Query("SELECT m FROM MomoPayment m WHERE m.orderId = :orderId")
    Optional<MomoPayment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);

    @Query("SELECT m FROM MomoPayment m WHERE m.orderId IN :orderIds")
    List<MomoPayment> findAllByOrderIds(List<String> orderIds);
}
