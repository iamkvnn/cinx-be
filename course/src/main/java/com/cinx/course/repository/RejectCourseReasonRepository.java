package com.cinx.course.repository;

import com.cinx.course.model.RejectCourseReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RejectCourseReasonRepository extends JpaRepository<RejectCourseReason, String> {
    @Query("""
        SELECT r
        FROM RejectCourseReason r
        WHERE r.courseId = :courseId
    """)
    Optional<RejectCourseReason> findByCourse(@Param("courseId") String courseId);

    @Modifying
    @Query("""
        DELETE FROM RejectCourseReason r
        WHERE r.courseId = :courseId
    """)
    void deleteForCourse(@Param("courseId") String courseId);
}
