package com.cinx.learning.service.video;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.SubmitVideoQuestionRequest;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.InVideoAssessmentSubmissionResponse;
import com.cinx.learning.dto.response.VideoLessonResponse;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import com.cinx.learning.mapper.VideoLessonTrackingHistoryMapper;
import com.cinx.learning.model.InVideoAssessmentSubmission;
import com.cinx.learning.model.VideoLessonTrackingHistory;
import com.cinx.learning.repository.InVideoAssessmentSubmissionRepository;
import com.cinx.learning.repository.VideoLessonTrackingHistoryRepository;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService implements IVideoService {
    private final VideoLessonTrackingHistoryMapper videoLessonTrackingHistoryMapper;
    private final VideoLessonTrackingHistoryRepository videoLessonTrackingHistoryRepository;
    private final CourseService courseService;
    private final ILearningProgressService learningProgressService;
    private final InVideoAssessmentSubmissionRepository inVideoAssessmentSubmissionRepository;

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
        
        VideoLessonResponse videoLessonResponse = courseService.getVideoLessonById(request.videoLessonId()).data();
        if (videoLessonResponse != null) {
            checkAndMarkVideoCompletion(userId, request.videoLessonId(), videoLessonResponse);
        }
    }

    @Override
    @Transactional
    public void submitVideoQuestionAnswer(String userId, SubmitVideoQuestionRequest request) {
        ApiResponse<Boolean> checkResult = courseService.checkVideoQuestionAnswer(request.videoAssessmentId(), request.userAnswer());
        if (checkResult == null || !Boolean.TRUE.equals(checkResult.data())) {
            throw new com.cinx.common.exception.BadRequestException("Incorrect answer. Please try again.");
        }

        InVideoAssessmentSubmission submission = inVideoAssessmentSubmissionRepository
                .findByUserIdAndVideoAssessmentId(userId, request.videoAssessmentId())
                .orElseGet(() -> InVideoAssessmentSubmission.builder()
                        .userId(userId)
                        .videoLessonId(request.videoLessonId())
                        .videoAssessmentId(request.videoAssessmentId())
                        .build());
                        
        submission.setUserAnswer(request.userAnswer());
        submission.setSubmissionTime(LocalDateTime.now());
        inVideoAssessmentSubmissionRepository.save(submission);
        
        VideoLessonResponse videoLessonResponse = courseService.getVideoLessonById(request.videoLessonId()).data();
        if (videoLessonResponse != null) {
            checkAndMarkVideoCompletion(userId, request.videoLessonId(), videoLessonResponse);
        }
    }

    private void checkAndMarkVideoCompletion(String userId, String videoLessonId, VideoLessonResponse videoLessonResponse) {
        VideoLessonTrackingHistory trackingHistory = videoLessonTrackingHistoryRepository
                .findByUserIdAndVideoLessonId(userId, videoLessonId)
                .orElse(null);
                
        double progress = 0.0;
        if (trackingHistory != null && videoLessonResponse.duration() != null && videoLessonResponse.duration() > 0) {
            progress = (double) trackingHistory.getCurrentPosition() / videoLessonResponse.duration();
        }

        boolean watchedEnough = progress >= 0.95;
        boolean questionsCompleted = true;
        
        if (Boolean.TRUE.equals(videoLessonResponse.hasQuestions()) && videoLessonResponse.questionCount() != null && videoLessonResponse.questionCount() > 0) {
            long answeredCount = inVideoAssessmentSubmissionRepository.countByUserIdAndVideoLessonId(userId, videoLessonId);
            questionsCompleted = answeredCount >= videoLessonResponse.questionCount();
        }

        if (watchedEnough && questionsCompleted) {
            learningProgressService.updateLearningItemProgress(userId, videoLessonId, new UpdateLearningItemRequest(true, true, 10.0));
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<InVideoAssessmentSubmissionResponse> getVideoQuestionSubmissions(String userId, String videoLessonId) {
        return inVideoAssessmentSubmissionRepository.findByUserIdAndVideoLessonId(userId, videoLessonId).stream()
                .map(sub -> new InVideoAssessmentSubmissionResponse(
                        sub.getVideoLessonId(),
                        sub.getVideoAssessmentId(),
                        sub.getUserAnswer(),
                        sub.getSubmissionTime()
                ))
                .toList();
    }
}
