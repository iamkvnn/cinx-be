package com.cinx.course.repository;

import com.cinx.course.model.Section;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, String> {
    @Query("""
        SELECT s
        FROM Section s
        JOIN s.course c
        WHERE c.id = :courseId
        ORDER BY s.orderIndex ASC, s.stableId ASC
    """)
    List<Section> findPublishedByCourse(@Param("courseId") String courseId);

    @Query("""
        SELECT s
        FROM Section s
        JOIN s.draft d
        WHERE d.id = :draftId
        ORDER BY s.orderIndex ASC, s.stableId ASC
    """)
    List<Section> findDraftByDraft(@Param("draftId") String draftId);

    @Query("""
        SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
        FROM Section s
        JOIN s.draft d
        WHERE d.id = :draftId
    """)
    boolean existsByDraft(@Param("draftId") String draftId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM Section s
        JOIN s.draft d
        WHERE d.id = :draftId
        ORDER BY s.orderIndex ASC, s.stableId ASC
    """)
    List<Section> findDraftByDraftForUpdate(@Param("draftId") String draftId);

    @Query("""
        SELECT s
        FROM Section s
        JOIN s.draft d
        WHERE d.id = :draftId
            AND s.stableId = :stableId
    """)
    Optional<Section> findDraftSection(@Param("draftId") String draftId, @Param("stableId") String stableId);

    @Modifying
    @Query("""
        DELETE FROM Section s
        WHERE s.course.id = :courseId
    """)
    void deletePublishedByCourse(@Param("courseId") String courseId);
}
