package com.cinx.user.repository;

import com.cinx.user.consts.PolicyStatus;
import com.cinx.user.consts.PolicyType;
import com.cinx.user.model.PolicyDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, String> {
    @Query("SELECT COALESCE(MAX(p.versionNumber), 0) FROM PolicyDocument p WHERE p.slug = :slug")
    Integer findMaxVersionNumberBySlug(String slug);

    Optional<PolicyDocument> findFirstBySlugAndStatusOrderByVersionNumberDesc(String slug, PolicyStatus status);

    List<PolicyDocument> findAllByStatusOrderByDisplayOrderAscPublishedAtDesc(PolicyStatus status);

    List<PolicyDocument> findAllBySlugAndStatus(String slug, PolicyStatus status);

    @Query("""
        SELECT p FROM PolicyDocument p
        WHERE (:status IS NULL OR p.status = :status)
          AND (:policyType IS NULL OR p.policyType = :policyType)
          AND (:query IS NULL OR p.title LIKE %:query% OR p.slug LIKE %:query% OR p.summary LIKE %:query%)
    """)
    Page<PolicyDocument> findAllForManagement(PolicyStatus status, PolicyType policyType, String query, Pageable pageable);
}
