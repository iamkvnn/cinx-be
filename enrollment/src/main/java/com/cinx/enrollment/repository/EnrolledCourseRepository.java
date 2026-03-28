package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.EnrolledCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrolledCourseRepository extends JpaRepository<EnrolledCourse, String> {
     boolean existsByCourseIdAndUserId(String courseId, String userId);

    Page<EnrolledCourse> findAllByUserId(String userId, Pageable pageable);

    List<EnrolledCourse> findAllByUserIdAndCourseIdIn(String userId, List<String> courseIds);
}
