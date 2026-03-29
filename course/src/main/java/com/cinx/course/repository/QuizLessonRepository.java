package com.cinx.course.repository;

import com.cinx.course.model.QuizLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizLessonRepository extends JpaRepository<QuizLesson, String> {
    Optional<QuizLesson> findByLessonId(String lessonId);
}
