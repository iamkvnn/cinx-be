package com.cinx.course.repository;

import com.cinx.course.model.QuizOption;
import com.cinx.course.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, String> {
    void deleteAllByQuizQuestionId(String quizQuestionId);
}
