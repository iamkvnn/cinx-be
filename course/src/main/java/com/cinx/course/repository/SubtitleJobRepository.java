package com.cinx.course.repository;

import com.cinx.course.consts.SubtitleJobStatus;
import com.cinx.course.model.SubtitleJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubtitleJobRepository extends JpaRepository<SubtitleJob, String> {
    List<SubtitleJob> findByVideoLessonLessonIdOrderByCreatedAtDesc(String lessonId);

    Optional<SubtitleJob> findByIdAndVideoLessonLessonId(String id, String lessonId);

    boolean existsByVideoLessonLessonIdAndTargetLanguageCodeAndStatusIn(
            String lessonId,
            String targetLanguageCode,
            List<SubtitleJobStatus> statuses
    );
}
