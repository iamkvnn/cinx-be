package com.cinx.course.repository;

import com.cinx.course.model.VideoLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoLessonRepository extends JpaRepository<VideoLesson, String> {
    Optional<VideoLesson> findByLessonId(String lessonId);
}
