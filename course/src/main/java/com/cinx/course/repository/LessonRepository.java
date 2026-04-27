package com.cinx.course.repository;

import com.cinx.course.consts.LessonType;
import com.cinx.course.model.Lesson;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, String> {
    List<Lesson> findAllBySectionIdIn(List<String> sectionIds);

    List<Lesson> findAllBySectionId(String sectionId);

    Optional<Lesson> findByIdAndSectionId(String lessonId, String sectionId);

    Optional<Lesson> findByIdAndLessonType(String lessonId, LessonType lessonType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lesson l WHERE l.id = :lessonId")
    Optional<Lesson> findByIdForUpdate(@Param("lessonId") String lessonId);
}
