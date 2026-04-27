package com.cinx.course.repository;

import com.cinx.course.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, String> {
    List<Section> findByCourseId(String courseId);

    Optional<Section> findByIdAndCourseId(String sectionId, String courseId);
}
