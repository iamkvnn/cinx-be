package com.cinx.course.repository;

import com.cinx.course.model.RejectCourseReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RejectCourseReasonRepository extends JpaRepository<RejectCourseReason, String> {
    Optional<RejectCourseReason> findByCourseId(String courseId);

    @Modifying
    @Query("DELETE FROM RejectCourseReason r WHERE r.courseId = :courseId")
    void deleteByCourseId(String courseId);
}
