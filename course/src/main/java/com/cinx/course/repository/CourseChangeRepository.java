package com.cinx.course.repository;

import com.cinx.course.model.CourseChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseChangeRepository extends JpaRepository<CourseChange, String> {
    Optional<CourseChange> findByCourseIdAndItemIdIsNull(String courseId);
    Optional<CourseChange> findByCourseIdAndItemId(String courseId, String itemId);
    List<CourseChange> findAllByCourseId(String courseId);

    @Modifying
    @Query("DELETE FROM CourseChange c WHERE c.courseId = :courseId")
    void deleteAllByCourseId(String courseId);
}
