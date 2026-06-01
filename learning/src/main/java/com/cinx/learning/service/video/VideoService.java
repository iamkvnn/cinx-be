package com.cinx.learning.service.video;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.consts.DailyGoalType;
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
import com.cinx.learning.service.activity.ILearningActivityService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
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
    private final ILearningActivityService learningActivityService;
    private final ILearningProgressService learningProgressService;
    private final IDailyGoalService dailyGoalService;
    private final InVideoAssessmentSubmissionRepository inVideoAssessmentSubmissionRepository;

    @Override
    public Page<VideoLessonTrackingHistoryResponse> getVideoLessonTrackingHistories(String courseId, String lessonId, int page, int size) {
        getVideoLesson(courseId, lessonId);
        return videoLessonTrackingHistoryRepository.findByVideoLessonId(lessonId, PageRequest.of(page - 1, size))
                .map(videoLessonTrackingHistoryMapper::toDto);
    }

    @Override
    public VideoLessonTrackingHistoryResponse getVideoLessonTrackingHistory(String courseId, String userId, String lessonId) {
        getVideoLesson(courseId, lessonId);
        return videoLessonTrackingHistoryRepository.findByUserIdAndVideoLessonId(userId, lessonId)
                .map(videoLessonTrackingHistoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Tracking history not found for userId: " + userId + " and videoLessonId: " + lessonId));
    }

    @Override
    public void trackVideoProgress(String courseId, String lessonId, String userId, TrackingVideoLessonRequest request) {
        VideoLessonResponse videoLessonResponse = getVideoLesson(courseId, lessonId);
        if (request.currentPosition() == null || request.currentPosition() < 0) {
            throw new BadRequestException("Current position must be a non-negative integer.");
        }
        Integer previousPosition = videoLessonTrackingHistoryRepository.findByUserIdAndVideoLessonId(userId, lessonId)
                .map(VideoLessonTrackingHistory::getCurrentPosition)
                .orElse(0);
        VideoLessonTrackingHistory trackingHistory = videoLessonTrackingHistoryRepository.findByUserIdAndVideoLessonId(userId, lessonId)
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
                    newTrackingHistory.setVideoLessonId(lessonId);
                    newTrackingHistory.setLastTrackingTime(LocalDateTime.now());
                    return newTrackingHistory;
                });
        videoLessonTrackingHistoryRepository.save(trackingHistory);
        recordVideoActivity(userId, courseId, previousPosition, request.currentPosition());
        
        if (videoLessonResponse != null) {
            checkAndMarkVideoCompletion(userId, lessonId, videoLessonResponse);
        }
    }

    private void recordVideoActivity(String userId, String courseId, Integer previousPosition, Integer currentPosition) {
        int activeSeconds = currentPosition - (previousPosition != null ? previousPosition : 0);
        if (activeSeconds <= 0) {
            return;
        }
        learningActivityService.recordActivity(userId, courseId, activeSeconds);
    }

    @Override
    @Transactional
    public void submitVideoQuestionAnswer(String courseId, String lessonId, String userId, SubmitVideoQuestionRequest request) {
        VideoLessonResponse videoLessonResponse = getVideoLesson(courseId, lessonId);
        ApiResponse<Boolean> checkResult = courseService.checkVideoQuestionAnswer(
                request.videoAssessmentId(),
                request.userAnswer()
        );
        if (checkResult == null || !Boolean.TRUE.equals(checkResult.data())) {
            throw new BadRequestException("Incorrect answer. Please try again.");
        }

        InVideoAssessmentSubmission submission = inVideoAssessmentSubmissionRepository
                .findByUserIdAndVideoAssessmentId(userId, request.videoAssessmentId())
                .orElseGet(() -> InVideoAssessmentSubmission.builder()
                        .userId(userId)
                        .videoLessonId(lessonId)
                        .videoAssessmentId(request.videoAssessmentId())
                        .build());
                        
        submission.setUserAnswer(request.userAnswer());
        submission.setSubmissionTime(LocalDateTime.now());
        inVideoAssessmentSubmissionRepository.save(submission);
        
        if (videoLessonResponse != null) {
            checkAndMarkVideoCompletion(userId, lessonId, videoLessonResponse);
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
            boolean wasCompleted = learningProgressService.isLearningItemCompleted(userId, videoLessonId);
            learningProgressService.updateLearningItemProgress(userId, videoLessonId, new UpdateLearningItemRequest(true, true, 10.0));
            if (!wasCompleted) {
                dailyGoalService.recordProgress(userId, DailyGoalType.VIDEOS_COMPLETED, 1);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InVideoAssessmentSubmissionResponse> getVideoQuestionSubmissions(String courseId, String userId, String lessonId) {
        getVideoLesson(courseId, lessonId);
        return inVideoAssessmentSubmissionRepository.findByUserIdAndVideoLessonId(userId, lessonId).stream()
                .map(sub -> new InVideoAssessmentSubmissionResponse(
                        sub.getVideoLessonId(),
                        sub.getVideoAssessmentId(),
                        sub.getUserAnswer(),
                        sub.getSubmissionTime()
                ))
                .toList();
    }

    private VideoLessonResponse getVideoLesson(String courseId, String lessonId) {
        return courseService.getVideoLessonById(courseId, lessonId).data();
    }

}
