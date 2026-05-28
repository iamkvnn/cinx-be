package com.cinx.learning.repository;

import com.cinx.learning.model.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, String> {
    List<CourseProgress> findAllByUserIdAndCourseIdIn(String userId, List<String> courseIds);

    Optional<CourseProgress> findByUserIdAndCourseId(String userId, String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    List<CourseProgress> findAllByCourseId(String courseId);
}
