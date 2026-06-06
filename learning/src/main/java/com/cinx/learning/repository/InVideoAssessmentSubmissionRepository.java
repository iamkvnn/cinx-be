package com.cinx.learning.repository;

import com.cinx.learning.model.InVideoAssessmentSubmission;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InVideoAssessmentSubmissionRepository extends JpaRepository<InVideoAssessmentSubmission, String> {
    long countByUserIdAndVideoLessonId(String userId, String videoLessonId);
    boolean existsByUserIdAndVideoAssessmentId(String userId, String videoAssessmentId);
    Optional<InVideoAssessmentSubmission> findByUserIdAndVideoAssessmentId(String userId, String videoAssessmentId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InVideoAssessmentSubmission s WHERE s.userId = :userId AND s.videoAssessmentId = :videoAssessmentId")
    Optional<InVideoAssessmentSubmission> findForUpdateByUserIdAndVideoAssessmentId(String userId, String videoAssessmentId);
    List<InVideoAssessmentSubmission> findByUserIdAndVideoLessonId(String userId, String videoLessonId);
}
