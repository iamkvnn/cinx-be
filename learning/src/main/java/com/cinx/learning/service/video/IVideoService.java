package com.cinx.learning.service.video;

import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import org.springframework.data.domain.Page;

public interface IVideoService {
    Page<VideoLessonTrackingHistoryResponse> getVideoLessonTrackingHistories(String videoLessonId, int page, int size);
    VideoLessonTrackingHistoryResponse getVideoLessonTrackingHistory(String userId, String videoLessonId);
    void trackVideoProgress(String userId, TrackingVideoLessonRequest request);
}
