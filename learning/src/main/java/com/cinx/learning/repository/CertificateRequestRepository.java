package com.cinx.learning.repository;

import com.cinx.learning.consts.CertificateStatus;
import com.cinx.learning.model.CertificateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, String> {
    Optional<CertificateRequest> findByUserIdAndCourseId(String userId, String courseId);
    Page<CertificateRequest> findByCourseId(String courseId, Pageable pageable);
    Page<CertificateRequest> findByCourseIdAndStatus(String courseId, CertificateStatus status, Pageable pageable);
    List<CertificateRequest> findByUserIdAndStatus(String userId, CertificateStatus status);
}