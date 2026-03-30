package com.cinx.learning.repository;

import com.cinx.learning.model.LearningPathItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, String> {
    List<LearningPathItem> findByLearningPathIdOrderByOrderIndexAsc(String learningPathId);
    Optional<LearningPathItem> findFirstByLearningPathIdAndIsCompletedFalseOrderByOrderIndexAsc(String learningPathId);
    Optional<LearningPathItem> findByLearningPathIdAndLessonId(String learningPathId, String lessonId);
}
