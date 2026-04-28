package com.cinx.learning.service.video;

import com.cinx.learning.dto.request.SubmitVideoQuestionRequest;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.response.InVideoAssessmentSubmissionResponse;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IVideoService {
    Page<VideoLessonTrackingHistoryResponse> getVideoLessonTrackingHistories(String videoLessonId, int page, int size);
    VideoLessonTrackingHistoryResponse getVideoLessonTrackingHistory(String userId, String videoLessonId);
    void trackVideoProgress(String userId, TrackingVideoLessonRequest request);
    void submitVideoQuestionAnswer(String userId, SubmitVideoQuestionRequest request);
    List<InVideoAssessmentSubmissionResponse> getVideoQuestionSubmissions(String userId, String videoLessonId);
}
