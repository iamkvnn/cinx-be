package com.cinx.course.repository;

import com.cinx.course.model.CourseDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseDraftRepository extends JpaRepository<CourseDraft, String> {
    @Query("""
        SELECT d
        FROM CourseDraft d
        JOIN d.course c
        WHERE c.id = :courseId
    """)
    Optional<CourseDraft> findByCourse(@Param("courseId") String courseId);
}
