package com.cinx.course.repository;

import com.cinx.course.model.ArticleLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleLessonRepository extends JpaRepository<ArticleLesson, String> {
    Optional<ArticleLesson> findByLessonId(String lessonId);
}
