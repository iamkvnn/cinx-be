package com.cinx.course.repository;

import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, String> {
    @Query("""
        SELECT c
        FROM Course c
        WHERE
            (:query IS NULL OR
                c.title LIKE %:query%)
    """)
    Page<Course> findAll(
            @Param("query") String query,
            Pageable pageable
    );

}
