package com.cinx.payment.repository;

import com.cinx.payment.model.VNPayPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VNPayPaymentRepository extends JpaRepository<VNPayPayment, String> {
   Optional<VNPayPayment> findByOrderId(String orderId);
}
