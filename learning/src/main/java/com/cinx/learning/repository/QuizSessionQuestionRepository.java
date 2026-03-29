package com.cinx.learning.repository;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.model.QuizSessionQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuizSessionQuestionRepository extends JpaRepository<QuizSessionQuestion, String> {
    Page<QuizSessionQuestion> findAllByQuizSessionId(String quizSessionId, Pageable pageable);

    Optional<QuizSessionQuestion> findByQuizSessionIdAndQuestionId(String quizSessionId, String questionId);
}
