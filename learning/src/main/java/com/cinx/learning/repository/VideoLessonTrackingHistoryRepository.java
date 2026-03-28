package com.cinx.learning.repository;

import com.cinx.learning.model.VideoLessonTrackingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoLessonTrackingHistoryRepository extends JpaRepository<VideoLessonTrackingHistory, String> {
    Page<VideoLessonTrackingHistory> findByVideoLessonId(String videoLessonId, Pageable pageable);

    Optional<VideoLessonTrackingHistory> findByUserIdAndVideoLessonId(String userId, String videoLessonId);
}
