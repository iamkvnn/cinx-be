package com.cinx.learning.repository;

import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.model.QuizSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, String> {

    @Query("""
        SELECT qs FROM QuizSession qs
        LEFT JOIN FETCH qs.quizSessionSubmission qss
        WHERE qs.quizLessonId = :quizLessonId
        AND (:userId IS NULL OR qs.userId = :userId)
    """)
    Page<QuizSession> findAllByQuizLessonId(String quizLessonId, String userId, Pageable pageable);

    Integer countByQuizLessonId(String quizLessonId);

    Page<QuizSession> findAllByQuizLessonIdAndStatus(String quizLessonId, QuizSessionStatus status, Pageable pageable);
}

