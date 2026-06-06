package com.cinx.learning.repository;

import com.cinx.learning.model.QuizSession;
import com.cinx.learning.consts.QuizSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizSessionRepository extends JpaRepository<QuizSession, String> {

    @Query("""
        SELECT qs FROM QuizSession qs
        LEFT JOIN FETCH qs.quizSessionSubmission qss
        WHERE qs.quizLessonId = :quizLessonId
        AND (:userId IS NULL OR qs.userId = :userId)
    """)
    Page<QuizSession> findAllByQuizLessonId(String quizLessonId, String userId, Pageable pageable);

    Integer countByQuizLessonIdAndUserId(String quizLessonId, String userId);

    boolean existsByQuizLessonIdAndUserIdAndStatus(String quizLessonId, String userId, QuizSessionStatus status);

    @Query("""
        SELECT qs FROM QuizSession qs
        WHERE qs.status = :status
        AND qs.endTime < :now
    """)
    List<QuizSession> findExpiredSessions(QuizSessionStatus status, LocalDateTime now);
}

