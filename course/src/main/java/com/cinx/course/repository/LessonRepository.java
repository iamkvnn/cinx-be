package com.cinx.course.repository;

import com.cinx.course.model.Lesson;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, String> {
    @Query("""
        SELECT l
        FROM Lesson l
        JOIN l.section s
        WHERE s.id = :sectionId
            AND l.stableId = :stableId
    """)
    Optional<Lesson> findBySectionAndStableId(@Param("sectionId") String sectionId, @Param("stableId") String stableId);

    @Query("""
        SELECT l FROM Lesson l
        JOIN l.section s
        JOIN s.course c
        WHERE c.id = :courseId AND c.isPublished = true
        ORDER BY s.orderIndex ASC, s.stableId ASC, l.orderIndex ASC, l.stableId ASC
    """)
    List<Lesson> findPublishedByCourse(@Param("courseId") String courseId);

    @Query("""
        SELECT l
        FROM Lesson l
        JOIN l.section s
        JOIN s.draft d
        WHERE d.id = :draftId
        ORDER BY s.orderIndex ASC, s.stableId ASC, l.orderIndex ASC, l.stableId ASC
    """)
    List<Lesson> findByDraft(@Param("draftId") String draftId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT l
        FROM Lesson l
        JOIN l.section s
        WHERE s.id IN :sectionIds
        ORDER BY s.orderIndex ASC, s.stableId ASC, l.orderIndex ASC, l.stableId ASC
    """)
    List<Lesson> findBySectionIdsForUpdate(@Param("sectionIds") List<String> sectionIds);

    @Query("""
        SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
        FROM Lesson l
        JOIN l.section s
        LEFT JOIN s.course c
        LEFT JOIN s.draft d
        LEFT JOIN d.course dc
        WHERE l.stableId = :lessonId
        AND (c.instructorId = :userId OR dc.instructorId = :userId)
    """)
    boolean isAccessibleByInstructor(@Param("lessonId") String lessonId, @Param("userId") String userId);

    @Query("""
        SELECT l FROM Lesson l
        JOIN l.section s
        JOIN s.draft d
        WHERE d.id = :draftId
        ORDER BY s.orderIndex ASC, s.stableId ASC, l.orderIndex ASC, l.stableId ASC
    """)
    List<Lesson> findDraftByDraft(@Param("draftId") String draftId);
}
