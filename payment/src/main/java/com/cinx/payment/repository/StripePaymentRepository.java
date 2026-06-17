package com.cinx.payment.repository;

import com.cinx.payment.model.StripePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StripePaymentRepository extends JpaRepository<StripePayment, String> {
    Optional<StripePayment> findByOrderId(String orderId);
    boolean existsByStripeEventId(String stripeEventId);

    @Query("SELECT s FROM StripePayment s WHERE s.orderId IN :orderIds")
    List<StripePayment> findAllByOrderIds(List<String> orderIds);
}
