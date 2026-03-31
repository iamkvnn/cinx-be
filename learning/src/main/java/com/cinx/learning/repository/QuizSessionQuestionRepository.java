package com.cinx.learning.repository;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.model.QuizSessionQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

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
}
