package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.EnrolledCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EnrolledCourseRepository extends JpaRepository<EnrolledCourse, String> {
     boolean existsByCourseIdAndUserId(String courseId, String userId);

    Page<EnrolledCourse> findAllByUserId(String userId, Pageable pageable);

    List<EnrolledCourse> findAllByUserIdAndCourseIdIn(String userId, List<String> courseIds);

    @Query("SELECT e.userId FROM EnrolledCourse e WHERE e.courseId = :courseId")
    List<String> findUserIdsByCourseId(String courseId);

    @Query("""
        SELECT e.courseId, COUNT(e)
        FROM EnrolledCourse e
        WHERE e.createdAt BETWEEN :start AND :end
        GROUP BY e.courseId
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> findTopEnrolledCourses(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
