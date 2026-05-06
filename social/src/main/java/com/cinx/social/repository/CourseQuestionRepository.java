package com.cinx.social.repository;

import com.cinx.social.model.CourseQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, String> {
    Page<CourseQuestion> findByCourseId(String courseId, Pageable pageable);
    Page<CourseQuestion> findByCourseIdAndLessonId(String courseId, String lessonId, Pageable pageable);
}
