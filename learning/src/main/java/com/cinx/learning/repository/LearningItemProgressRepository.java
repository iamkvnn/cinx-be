package com.cinx.learning.repository;

import com.cinx.learning.model.LearningItemProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LearningItemProgressRepository extends JpaRepository<LearningItemProgress, String> {
    @Query("SELECT lip FROM LearningItemProgress lip " +
            "JOIN lip.courseProgress cp " +
            "WHERE cp.userId = :userId AND cp.courseId = :courseId")
    List<LearningItemProgress> findAllByUserIdAndCourseId(String userId, String courseId);

    @Query("SELECT lip FROM LearningItemProgress lip " +
            "JOIN lip.courseProgress cp " +
            "WHERE cp.userId = :userId AND lip.itemId = :itemId")
    Optional<LearningItemProgress> findByItemIdAndUserId(String itemId, String userId);
}
