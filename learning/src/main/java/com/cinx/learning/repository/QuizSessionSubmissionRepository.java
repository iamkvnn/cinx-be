package com.cinx.learning.repository;

import com.cinx.learning.model.QuizSessionSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSessionSubmissionRepository extends JpaRepository<QuizSessionSubmission, String> {
    Optional<QuizSessionSubmission> findByQuizSessionId(String quizSessionId);
}
