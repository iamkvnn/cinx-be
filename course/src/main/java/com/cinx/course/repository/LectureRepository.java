package com.cinx.course.repository;

import com.cinx.course.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, String> {
    List<Lecture> findAllBySectionIdIn(List<String> sectionIds);
}
