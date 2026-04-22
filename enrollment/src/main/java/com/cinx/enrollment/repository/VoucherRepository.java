package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);

    @Query("""
        SELECT v FROM Voucher v
        WHERE (:query IS NULL OR v.code LIKE %:query% OR v.description LIKE %:query%)
    """)
    Page<Voucher> findAll(String query, Pageable pageable);
}
