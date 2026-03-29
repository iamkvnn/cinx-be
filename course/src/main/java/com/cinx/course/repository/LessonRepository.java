package com.cinx.course.repository;

import com.cinx.course.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, String> {
    List<Lesson> findAllBySectionIdIn(List<String> sectionIds);
}
