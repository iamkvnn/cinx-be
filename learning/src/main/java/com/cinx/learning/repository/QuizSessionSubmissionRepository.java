package com.cinx.learning.repository;

import com.cinx.learning.model.QuizSessionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuizSessionSubmissionRepository extends JpaRepository<QuizSessionSubmission, String> {
    Optional<QuizSessionSubmission> findByQuizSessionId(String quizSessionId);

    @Query("""
            SELECT s FROM QuizSessionSubmission s
            JOIN s.quizSession qs
            WHERE qs.userId = :userId
              AND qs.quizLessonId = :quizLessonId
            ORDER BY s.submissionTime ASC
            """)
    List<QuizSessionSubmission> findAllByUserIdAndQuizLessonId(
            @Param("userId") String userId,
            @Param("quizLessonId") String quizLessonId
    );

    @Query("""
            SELECT s FROM QuizSessionSubmission s
            JOIN FETCH s.quizSession qs
            WHERE qs.quizLessonId = :quizLessonId
            ORDER BY s.submissionTime ASC
            """)
    List<QuizSessionSubmission> findAllByQuizLessonId(String quizLessonId);

    List<QuizSessionSubmission> findAllByQuizSessionIdIn(Collection<String> strings);
}
