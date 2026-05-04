package com.cinx.learning.repository;

import com.cinx.learning.model.QuizSessionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSessionSubmissionRepository extends JpaRepository<QuizSessionSubmission, String> {
    Optional<QuizSessionSubmission> findByQuizSessionId(String quizSessionId);

    @Query("""
            SELECT s FROM QuizSessionSubmission s
            JOIN QuizSession qs ON qs.id = s.quizSessionId
            WHERE qs.userId = :userId
              AND qs.quizLessonId = :quizLessonId
            ORDER BY s.submissionTime ASC
            """)
    List<QuizSessionSubmission> findAllByUserIdAndQuizLessonId(
            @Param("userId") String userId,
            @Param("quizLessonId") String quizLessonId
    );
}
