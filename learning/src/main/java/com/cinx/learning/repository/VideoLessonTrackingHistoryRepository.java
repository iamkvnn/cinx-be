package com.cinx.learning.repository;

import com.cinx.learning.model.VideoLessonTrackingHistory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VideoLessonTrackingHistoryRepository extends JpaRepository<VideoLessonTrackingHistory, String> {
    Page<VideoLessonTrackingHistory> findByVideoLessonId(String videoLessonId, Pageable pageable);

    Optional<VideoLessonTrackingHistory> findByUserIdAndVideoLessonId(String userId, String videoLessonId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM VideoLessonTrackingHistory h WHERE h.userId = :userId AND h.videoLessonId = :videoLessonId")
    Optional<VideoLessonTrackingHistory> findForUpdateByUserIdAndVideoLessonId(String userId, String videoLessonId);
}
