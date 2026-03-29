package com.cinx.course.repository;

import com.cinx.course.model.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, String> {
}
