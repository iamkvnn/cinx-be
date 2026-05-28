package com.cinx.course.repository;

import com.cinx.course.model.CourseEnrollmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEnrollmentEventRepository extends JpaRepository<CourseEnrollmentEvent, String> {
}
