package com.cinx.course.repository;

import com.cinx.course.model.AssignmentLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentLessonRepository extends JpaRepository<AssignmentLesson, String> {
    Optional<AssignmentLesson> findByLessonId(String lessonId);
}
