package com.cinx.course.repository;

import com.cinx.course.model.SubtitleTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubtitleTrackRepository extends JpaRepository<SubtitleTrack, String> {
    List<SubtitleTrack> findByVideoLessonLessonIdOrderByIsDefaultDescLanguageCodeAsc(String lessonId);

    Optional<SubtitleTrack> findByIdAndVideoLessonLessonId(String id, String lessonId);

    Optional<SubtitleTrack> findByVideoLessonLessonIdAndLanguageCode(String lessonId, String languageCode);

    List<SubtitleTrack> findByVideoLessonLessonIdAndIsDefaultTrue(String lessonId);

    long countByVideoLessonLessonId(String lessonId);
}
