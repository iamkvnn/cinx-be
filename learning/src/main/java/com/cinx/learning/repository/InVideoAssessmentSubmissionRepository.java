package com.cinx.learning.repository;

import com.cinx.learning.model.InVideoAssessmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InVideoAssessmentSubmissionRepository extends JpaRepository<InVideoAssessmentSubmission, String> {
    long countByUserIdAndVideoLessonId(String userId, String videoLessonId);
    boolean existsByUserIdAndVideoAssessmentId(String userId, String videoAssessmentId);
    Optional<InVideoAssessmentSubmission> findByUserIdAndVideoAssessmentId(String userId, String videoAssessmentId);
    List<InVideoAssessmentSubmission> findByUserIdAndVideoLessonId(String userId, String videoLessonId);
}
