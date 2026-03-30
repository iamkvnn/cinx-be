package com.cinx.learning.repository;

import com.cinx.learning.consts.LearningPathStatus;
import com.cinx.learning.model.UserLearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLearningPathRepository extends JpaRepository<UserLearningPath, String> {
    List<UserLearningPath> findByUserId(String userId);
    Optional<UserLearningPath> findByUserIdAndStatus(String userId, LearningPathStatus status);
    Optional<UserLearningPath> findByUserIdAndStatusIn(String userId, List<LearningPathStatus> statuses);
}
