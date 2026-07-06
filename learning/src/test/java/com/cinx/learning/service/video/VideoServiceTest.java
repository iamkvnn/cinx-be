package com.cinx.learning.service.video;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.dto.request.SubmitVideoQuestionRequest;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.VideoLessonResponse;
import com.cinx.learning.mapper.VideoLessonTrackingHistoryMapper;
import com.cinx.learning.model.InVideoAssessmentSubmission;
import com.cinx.learning.model.VideoLessonTrackingHistory;
import com.cinx.learning.repository.InVideoAssessmentSubmissionRepository;
import com.cinx.learning.repository.VideoLessonTrackingHistoryRepository;
import com.cinx.learning.service.activity.ILearningActivityService;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.learningProgress.LearningItemProgressUpdateResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {
    @Mock
    private VideoLessonTrackingHistoryMapper videoLessonTrackingHistoryMapper;
    @Mock
    private VideoLessonTrackingHistoryRepository videoLessonTrackingHistoryRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private ILearningActivityService learningActivityService;
    @Mock
    private ILearningProgressService learningProgressService;
    @Mock
    private IDailyGoalService dailyGoalService;
    @Mock
    private InVideoAssessmentSubmissionRepository inVideoAssessmentSubmissionRepository;
    @Spy
    private WatchedRangeTracker watchedRangeTracker = new WatchedRangeTracker(new ObjectMapper());

    @InjectMocks
    private VideoService videoService;

    @Test
    void getVideoLessonTrackingHistoriesUsesSortParameter() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(courseService.getVideoLessonById("course-1", "video-1"))
                .thenReturn(new ApiResponse<>(true, "ok", videoLesson(100, false, 0)));
        when(videoLessonTrackingHistoryRepository.findByVideoLessonId(eq("video-1"), pageableCaptor.capture()))
                .thenReturn(Page.empty());

        videoService.getVideoLessonTrackingHistories("course-1", "video-1", 1, 10, "{\"lastTrackingTime\":\"DESC\"}");

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("lastTrackingTime");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void firstVideoTrackingCreditsInitialWatchedRange() {
        VideoLessonResponse lesson = videoLesson(13, false, 0);
        AtomicReference<VideoLessonTrackingHistory> savedHistory = new AtomicReference<>();
        when(courseService.getVideoLessonById("course-1", "video-1"))
                .thenReturn(new ApiResponse<>(true, "ok", lesson));
        when(videoLessonTrackingHistoryRepository.findForUpdateByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenAnswer(invocation -> Optional.ofNullable(savedHistory.get()));
        when(videoLessonTrackingHistoryRepository.save(any(VideoLessonTrackingHistory.class)))
                .thenAnswer(invocation -> {
                    VideoLessonTrackingHistory history = invocation.getArgument(0);
                    savedHistory.set(history);
                    return history;
                });
        when(learningProgressService.updateLearningItemProgress(
                "user-1",
                "video-1",
                new UpdateLearningItemRequest(true, true, 10.0)))
                .thenReturn(new LearningItemProgressUpdateResult(true, true, false, false));

        videoService.trackVideoProgress(
                "course-1",
                "video-1",
                "user-1",
                new TrackingVideoLessonRequest(13));

        assertEquals("[[0,13]]", savedHistory.get().getWatchedRanges());
        verify(learningProgressService).updateLearningItemProgress(
                "user-1",
                "video-1",
                new UpdateLearningItemRequest(true, true, 10.0));
        verify(dailyGoalService).recordProgress("user-1", DailyGoalType.VIDEOS_COMPLETED, 1);
    }

    @Test
    void videoDoesNotCompleteAtSeventyNinePercent() {
        VideoLessonResponse lesson = videoLesson(100, true, 1);
        VideoLessonTrackingHistory history = trackingHistory("[[0,79]]");
        when(courseService.getVideoLessonById("course-1", "video-1"))
                .thenReturn(new ApiResponse<>(true, "ok", lesson));
        when(courseService.checkVideoQuestionAnswer("assessment-1", "answer"))
                .thenReturn(new ApiResponse<>(true, "ok", true));
        when(inVideoAssessmentSubmissionRepository.findForUpdateByUserIdAndVideoAssessmentId("user-1", "assessment-1"))
                .thenReturn(Optional.empty());
        when(videoLessonTrackingHistoryRepository.findForUpdateByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenReturn(Optional.of(history));
        when(inVideoAssessmentSubmissionRepository.countByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenReturn(1L);

        videoService.submitVideoQuestionAnswer(
                "course-1",
                "video-1",
                "user-1",
                new SubmitVideoQuestionRequest("assessment-1", "answer"));

        verify(learningProgressService, never()).updateLearningItemProgress(any(), any(), any());
        verify(dailyGoalService, never()).recordProgress("user-1", DailyGoalType.VIDEOS_COMPLETED, 1);
    }

    @Test
    void videoCompletesAtEightyPercentWithAllQuestionsCorrect() {
        VideoLessonResponse lesson = videoLesson(100, true, 1);
        VideoLessonTrackingHistory history = trackingHistory("[[0,80]]");
        when(courseService.getVideoLessonById("course-1", "video-1"))
                .thenReturn(new ApiResponse<>(true, "ok", lesson));
        when(courseService.checkVideoQuestionAnswer("assessment-1", "answer"))
                .thenReturn(new ApiResponse<>(true, "ok", true));
        when(inVideoAssessmentSubmissionRepository.findForUpdateByUserIdAndVideoAssessmentId("user-1", "assessment-1"))
                .thenReturn(Optional.empty());
        when(videoLessonTrackingHistoryRepository.findForUpdateByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenReturn(Optional.of(history));
        when(inVideoAssessmentSubmissionRepository.countByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenReturn(1L);
        when(learningProgressService.updateLearningItemProgress(
                "user-1",
                "video-1",
                new UpdateLearningItemRequest(true, true, 10.0)))
                .thenReturn(new LearningItemProgressUpdateResult(true, true, false, false));

        videoService.submitVideoQuestionAnswer(
                "course-1",
                "video-1",
                "user-1",
                new SubmitVideoQuestionRequest("assessment-1", "answer"));

        verify(dailyGoalService).recordProgress("user-1", DailyGoalType.VIDEOS_COMPLETED, 1);
    }

    @Test
    void repeatedVideoCompletionDoesNotIncrementVideoGoalAgain() {
        VideoLessonResponse lesson = videoLesson(100, true, 1);
        VideoLessonTrackingHistory history = trackingHistory("[[0,80]]");
        when(courseService.getVideoLessonById("course-1", "video-1"))
                .thenReturn(new ApiResponse<>(true, "ok", lesson));
        when(courseService.checkVideoQuestionAnswer("assessment-1", "answer"))
                .thenReturn(new ApiResponse<>(true, "ok", true));
        when(inVideoAssessmentSubmissionRepository.findForUpdateByUserIdAndVideoAssessmentId("user-1", "assessment-1"))
                .thenReturn(Optional.of(InVideoAssessmentSubmission.builder()
                        .userId("user-1")
                        .videoLessonId("video-1")
                        .videoAssessmentId("assessment-1")
                        .build()));
        when(videoLessonTrackingHistoryRepository.findForUpdateByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenReturn(Optional.of(history));
        when(inVideoAssessmentSubmissionRepository.countByUserIdAndVideoLessonId("user-1", "video-1"))
                .thenReturn(1L);
        when(learningProgressService.updateLearningItemProgress(
                "user-1",
                "video-1",
                new UpdateLearningItemRequest(true, true, 10.0)))
                .thenReturn(new LearningItemProgressUpdateResult(false, false, false, false));

        videoService.submitVideoQuestionAnswer(
                "course-1",
                "video-1",
                "user-1",
                new SubmitVideoQuestionRequest("assessment-1", "answer"));

        verify(dailyGoalService, never()).recordProgress("user-1", DailyGoalType.VIDEOS_COMPLETED, 1);
    }

    private VideoLessonResponse videoLesson(Integer duration, Boolean hasQuestions, Integer questionCount) {
        return new VideoLessonResponse(
                "https://video.example.test/video.mp4",
                "video.mp4",
                "video/mp4",
                100L,
                duration,
                "READY",
                hasQuestions,
                questionCount,
                false,
                0,
                null);
    }

    private VideoLessonTrackingHistory trackingHistory(String watchedRanges) {
        return VideoLessonTrackingHistory.builder()
                .userId("user-1")
                .videoLessonId("video-1")
                .currentPosition(80)
                .watchedRanges(watchedRanges)
                .lastTrackingTime(LocalDateTime.now())
                .build();
    }
}
