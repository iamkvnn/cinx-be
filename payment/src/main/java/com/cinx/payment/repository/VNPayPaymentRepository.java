package com.cinx.payment.repository;

import com.cinx.payment.model.VNPayPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VNPayPaymentRepository extends JpaRepository<VNPayPayment, String> {
   Optional<VNPayPayment> findByOrderId(String orderId);

   @Query("SELECT v FROM VNPayPayment v WHERE v.orderId IN :orderIds")
   List<VNPayPayment> findAllByOrderIds(List<String> orderIds);
}
