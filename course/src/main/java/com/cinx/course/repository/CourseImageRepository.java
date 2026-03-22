package com.cinx.course.repository;

import com.cinx.course.model.CourseImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseImageRepository extends JpaRepository<CourseImage, String> {
}
