package com.cinx.learning.repository;

import com.cinx.learning.model.AssignmentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, String> {
    Page<AssignmentSubmission> findAllByAssignmentId(String assignmentId, Pageable pageable);

    Optional<AssignmentSubmission> findByUserIdAndAssignmentId(String userId, String assignmentId);
}
