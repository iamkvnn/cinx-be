package com.cinx.learning.repository;

import com.cinx.learning.model.QuizScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizScoreHistoryRepository extends JpaRepository<QuizScoreHistory, String> {
}
