package com.cinx.learning.service.video;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import com.cinx.learning.mapper.VideoLessonTrackingHistoryMapper;
import com.cinx.learning.model.VideoLessonTrackingHistory;
import com.cinx.learning.repository.VideoLessonTrackingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VideoService implements IVideoService {
    private final VideoLessonTrackingHistoryMapper videoLessonTrackingHistoryMapper;
    private final VideoLessonTrackingHistoryRepository videoLessonTrackingHistoryRepository;

    @Override
    public Page<VideoLessonTrackingHistoryResponse> getVideoLessonTrackingHistories(String videoLessonId, int page, int size) {
        return videoLessonTrackingHistoryRepository.findByVideoLessonId(videoLessonId, PageRequest.of(page - 1, size))
                .map(videoLessonTrackingHistoryMapper::toDto);
    }

    @Override
    public VideoLessonTrackingHistoryResponse getVideoLessonTrackingHistory(String userId, String videoLessonId) {
        return videoLessonTrackingHistoryRepository.findByUserIdAndVideoLessonId(userId, videoLessonId)
                .map(videoLessonTrackingHistoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Tracking history not found for userId: " + userId + " and videoLessonId: " + videoLessonId));
    }

    @Override
    public void trackVideoProgress(String userId, TrackingVideoLessonRequest request) {
        if (request.currentPosition() == null || request.currentPosition() < 0) {
            throw new BadRequestException("Current position must be a non-negative integer.");
        }
        if (request.videoLessonId() == null || request.videoLessonId().isEmpty()) {
            throw new BadRequestException("Video lesson ID must not be null or empty.");
        }
        VideoLessonTrackingHistory trackingHistory = videoLessonTrackingHistoryRepository.findByUserIdAndVideoLessonId(userId, request.videoLessonId())
                .map(existing -> {
                    if (request.currentPosition() < existing.getCurrentPosition()) {
                        return existing; // Do not update if the new position is less than the existing position
                    }
                    if (existing.getLastTrackingTime().plusSeconds(request.currentPosition() - existing.getCurrentPosition()).isAfter(LocalDateTime.now())) {
                        throw new BadRequestException("Tracking update is too frequent. Please wait before updating again.");
                    }
                    videoLessonTrackingHistoryMapper.partialUpdate(existing, request);
                    existing.setLastTrackingTime(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> {
                    VideoLessonTrackingHistory newTrackingHistory = videoLessonTrackingHistoryMapper.toModel(request);
                    newTrackingHistory.setUserId(userId);
                    newTrackingHistory.setLastTrackingTime(LocalDateTime.now());
                    return newTrackingHistory;
                });
        videoLessonTrackingHistoryRepository.save(trackingHistory);
    }
}
