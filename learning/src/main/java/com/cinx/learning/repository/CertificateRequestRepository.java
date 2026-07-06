package com.cinx.learning.repository;

import com.cinx.learning.consts.CertificateStatus;
import com.cinx.learning.model.CertificateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, String> {
    Optional<CertificateRequest> findByUserIdAndCourseId(String userId, String courseId);
    Page<CertificateRequest> findByCourseId(String courseId, Pageable pageable);
    Page<CertificateRequest> findByCourseIdAndStatus(String courseId, CertificateStatus status, Pageable pageable);
    Page<CertificateRequest> findByStatus(CertificateStatus status, Pageable pageable);
    List<CertificateRequest> findByUserIdAndStatus(String userId, CertificateStatus status);

    @Query("""
        SELECT c
        FROM CertificateRequest c
        WHERE (:courseId IS NULL OR c.courseId = :courseId)
          AND (:status IS NULL OR c.status = :status)
          AND (:query IS NULL OR c.id LIKE %:query% OR c.userId LIKE %:query% OR c.courseId LIKE %:query%)
    """)
    Page<CertificateRequest> search(String courseId, CertificateStatus status, String query, Pageable pageable);

    @Query("""
        SELECT c
        FROM CertificateRequest c
        WHERE c.courseId IN :courseIds
          AND (:status IS NULL OR c.status = :status)
          AND (:query IS NULL OR c.id LIKE %:query% OR c.userId LIKE %:query% OR c.courseId LIKE %:query%)
    """)
    Page<CertificateRequest> searchByCourseIds(List<String> courseIds, CertificateStatus status, String query, Pageable pageable);
}
