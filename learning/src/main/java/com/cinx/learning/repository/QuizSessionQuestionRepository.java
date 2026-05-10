package com.cinx.learning.repository;

import com.cinx.learning.model.QuizSessionQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface QuizSessionQuestionRepository extends JpaRepository<QuizSessionQuestion, String> {
    Page<QuizSessionQuestion> findAllByQuizSessionId(String quizSessionId, Pageable pageable);

    Optional<QuizSessionQuestion> findByQuizSessionIdAndQuestionId(String quizSessionId, String questionId);

    @Query("SELECT q.questionId as questionId, COUNT(q) as totalAttempts, " +
           "SUM(CASE WHEN q.score > 0 THEN 1 ELSE 0 END) as correctAttempts " +
           "FROM QuizSessionQuestion q " +
           "JOIN q.quizSession s " +
           "WHERE s.quizLessonId = :quizId AND s.status = com.cinx.learning.consts.QuizSessionStatus.SUBMITTED " +
           "GROUP BY q.questionId")
    List<Object[]> getQuizAnalyticsByQuizId(String quizId);

    @Query("""
            SELECT q FROM QuizSessionQuestion q
            JOIN FETCH q.quizSession s
            WHERE q.questionId IN :questionIds
              AND s.quizLessonId = :quizLessonId
            """)
    List<QuizSessionQuestion> findAllByQuizLessonIdAndQuestionIdIn(
            String quizLessonId,
            List<String> questionIds
    );

    @Query("""
            SELECT q FROM QuizSessionQuestion q
            WHERE q.quizSessionId = :sessionId
              AND q.questionType = com.cinx.learning.consts.QuizQuestionType.ESSAY
            """)
    List<QuizSessionQuestion> findAllEssayByQuizSessionId(String sessionId);

    List<QuizSessionQuestion> findAllByQuizSessionIdIn(Collection<String> strings);
}
