package com.cinx.course.service.subtitle;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.course.consts.SubtitleFormat;
import com.cinx.course.consts.SubtitleJobStatus;
import com.cinx.course.consts.SubtitleJobType;
import com.cinx.course.consts.SubtitleSource;
import com.cinx.course.consts.SubtitleStatus;
import com.cinx.course.dto.request.GenerateDefaultSubtitleJobRequest;
import com.cinx.course.dto.request.SubtitleJobCompletedRequest;
import com.cinx.course.dto.request.TranslateSubtitleJobRequest;
import com.cinx.course.dto.response.SubtitleJobResponse;
import com.cinx.course.mapper.SubtitleJobMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.SubtitleGenerateRequestedEvent;
import com.cinx.course.messaging.event.SubtitleTranslateRequestedEvent;
import com.cinx.course.model.SubtitleJob;
import com.cinx.course.model.SubtitleTrack;
import com.cinx.course.model.VideoLesson;
import com.cinx.course.repository.SubtitleJobRepository;
import com.cinx.course.repository.SubtitleTrackRepository;
import com.cinx.course.repository.VideoLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.s3.S3Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubtitleJobServiceTest {
    @Mock
    private SubtitleJobRepository subtitleJobRepository;
    @Mock
    private SubtitleTrackRepository subtitleTrackRepository;
    @Mock
    private VideoLessonRepository videoLessonRepository;
    @Mock
    private SubtitleJobMapper subtitleJobMapper;
    @Spy
    private WhisperLanguageRegistry whisperLanguageRegistry;
    @Mock
    private CourseEventProducer courseEventProducer;
    @Mock
    private ILessonService lessonService;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private SubtitleJobService subtitleJobService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDefaultSubtitleJob_requiresNoExistingSubtitleAndPublishesEvent() {
        authenticate("instructor-1");
        VideoLesson videoLesson = videoLesson("lesson-1");
        when(videoLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(videoLesson));
        when(lessonService.isLessonInstructor("lesson-1", "instructor-1")).thenReturn(true);
        when(subtitleTrackRepository.countByVideoLessonLessonId("lesson-1")).thenReturn(0L);
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode("lesson-1", "vi")).thenReturn(Optional.empty());
        when(subtitleJobRepository.existsByVideoLessonLessonIdAndTargetLanguageCodeAndStatusIn(eq("lesson-1"), eq("vi"), anyList()))
                .thenReturn(false);
        when(subtitleJobRepository.save(any(SubtitleJob.class))).thenAnswer(invocation -> savedJob(invocation.getArgument(0), "job-1"));
        when(subtitleJobMapper.toDto(any(SubtitleJob.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        SubtitleJobResponse response = subtitleJobService.createDefaultSubtitleJob(
                "course-1",
                "lesson-1",
                new GenerateDefaultSubtitleJobRequest("vi", null)
        );

        assertThat(response.jobType()).isEqualTo(SubtitleJobType.GENERATE_DEFAULT);
        assertThat(response.status()).isEqualTo(SubtitleJobStatus.QUEUED);
        assertThat(response.targetLanguageCode()).isEqualTo("vi");
        assertThat(response.expectedOutputFileKey()).isEqualTo("courses/subtitles/ai/lesson-1/default.vtt");

        ArgumentCaptor<SubtitleGenerateRequestedEvent> eventCaptor = ArgumentCaptor.forClass(SubtitleGenerateRequestedEvent.class);
        verify(courseEventProducer).publishSubtitleGenerateRequestedEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().jobId()).isEqualTo(response.id());
        assertThat(eventCaptor.getValue().videoFileKey()).isEqualTo("videos/lesson-1.mp4");
    }

    @Test
    void createDefaultSubtitleJob_rejectsUnsupportedLanguageAndExistingSubtitle() {
        authenticate("instructor-1");
        when(videoLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(videoLesson("lesson-1")));
        when(lessonService.isLessonInstructor("lesson-1", "instructor-1")).thenReturn(true);
        when(subtitleTrackRepository.countByVideoLessonLessonId("lesson-1")).thenReturn(0L);

        assertThatThrownBy(() -> subtitleJobService.createDefaultSubtitleJob(
                "course-1",
                "lesson-1",
                new GenerateDefaultSubtitleJobRequest("zz", null)
        )).isInstanceOf(BadRequestException.class);

        when(subtitleTrackRepository.countByVideoLessonLessonId("lesson-1")).thenReturn(1L);
        assertThatThrownBy(() -> subtitleJobService.createDefaultSubtitleJob(
                "course-1",
                "lesson-1",
                new GenerateDefaultSubtitleJobRequest("vi", null)
        )).isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTranslationJobs_requiresReadyDefaultAndPublishesOneJobPerLanguage() {
        authenticate("instructor-1");
        VideoLesson videoLesson = videoLesson("lesson-1");
        SubtitleTrack source = subtitle("source-1", "vi", true);
        when(videoLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(videoLesson));
        when(lessonService.isLessonInstructor("lesson-1", "instructor-1")).thenReturn(true);
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndIsDefaultTrue("lesson-1")).thenReturn(List.of(source));
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode(eq("lesson-1"), any())).thenReturn(Optional.empty());
        when(subtitleJobRepository.existsByVideoLessonLessonIdAndTargetLanguageCodeAndStatusIn(eq("lesson-1"), any(), anyList()))
                .thenReturn(false);
        when(subtitleJobRepository.save(any(SubtitleJob.class))).thenAnswer(invocation -> {
            SubtitleJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId("job-" + job.getTargetLanguageCode());
            }
            return job;
        });
        when(subtitleJobMapper.toDto(any(SubtitleJob.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        List<SubtitleJobResponse> responses = subtitleJobService.createTranslationJobs(
                "course-1",
                "lesson-1",
                new TranslateSubtitleJobRequest(null, List.of("en", "fr"))
        );

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(SubtitleJobResponse::targetLanguageCode).containsExactly("en", "fr");
        ArgumentCaptor<SubtitleTranslateRequestedEvent> eventCaptor = ArgumentCaptor.forClass(SubtitleTranslateRequestedEvent.class);
        verify(courseEventProducer, times(2)).publishSubtitleTranslateRequestedEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).extracting(SubtitleTranslateRequestedEvent::sourceSubtitleId)
                .containsOnly("source-1");
    }

    @Test
    void createTranslationJobs_rejectsDuplicateOrExistingTargetLanguage() {
        authenticate("instructor-1");
        SubtitleTrack source = subtitle("source-1", "vi", true);
        when(videoLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(videoLesson("lesson-1")));
        when(lessonService.isLessonInstructor("lesson-1", "instructor-1")).thenReturn(true);
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndIsDefaultTrue("lesson-1")).thenReturn(List.of(source));

        assertThatThrownBy(() -> subtitleJobService.createTranslationJobs(
                "course-1",
                "lesson-1",
                new TranslateSubtitleJobRequest(null, List.of("en", "en"))
        )).isInstanceOf(BadRequestException.class);

        when(subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode("lesson-1", "en"))
                .thenReturn(Optional.of(subtitle("existing-1", "en", false)));
        assertThatThrownBy(() -> subtitleJobService.createTranslationJobs(
                "course-1",
                "lesson-1",
                new TranslateSubtitleJobRequest(null, List.of("en"))
        )).isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void callbacksUpdateProgressFailureAndCompletionCreatesSubtitle() {
        VideoLesson videoLesson = videoLesson("lesson-1");
        SubtitleJob job = job(videoLesson, SubtitleJobType.TRANSLATE, "en");
        when(subtitleJobRepository.findById("job-1")).thenReturn(Optional.of(job));
        when(subtitleJobRepository.save(any(SubtitleJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subtitleJobService.markProcessing("job-1", 45);
        assertThat(job.getStatus()).isEqualTo(SubtitleJobStatus.PROCESSING);
        assertThat(job.getProgressPercent()).isEqualTo(45);

        subtitleJobService.markFailed("job-1", "MODEL_ERROR", "Model failed");
        assertThat(job.getStatus()).isEqualTo(SubtitleJobStatus.FAILED);
        assertThat(job.getErrorCode()).isEqualTo("MODEL_ERROR");

        SubtitleJob completedJob = job(videoLesson, SubtitleJobType.TRANSLATE, "en");
        when(subtitleJobRepository.findById("job-2")).thenReturn(Optional.of(completedJob));
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode("lesson-1", "en")).thenReturn(Optional.empty());
        when(s3Service.publicUrl(completedJob.getExpectedOutputFileKey()))
                .thenReturn("https://cdn.example.com/" + completedJob.getExpectedOutputFileKey());
        when(subtitleTrackRepository.save(any(SubtitleTrack.class))).thenAnswer(invocation -> {
            SubtitleTrack track = invocation.getArgument(0);
            track.setId("subtitle-1");
            return track;
        });

        subtitleJobService.markCompleted(new SubtitleJobCompletedRequest(
                "job-2",
                completedJob.getExpectedOutputFileKey(),
                null,
                "job-2.vtt",
                "text/vtt",
                2048L,
                "en",
                null,
                "courses/subtitles/ai/lesson-1/en.words.json",
                "https://cdn.example.com/courses/subtitles/ai/lesson-1/en.words.json"
        ));

        ArgumentCaptor<SubtitleTrack> trackCaptor = ArgumentCaptor.forClass(SubtitleTrack.class);
        verify(subtitleTrackRepository).save(trackCaptor.capture());
        assertThat(trackCaptor.getValue().getSource()).isEqualTo(SubtitleSource.AI_TRANSLATED);
        assertThat(trackCaptor.getValue().getFormat()).isEqualTo(SubtitleFormat.VTT);
        assertThat(trackCaptor.getValue().getIsDefault()).isFalse();
        assertThat(trackCaptor.getValue().getWordConfidenceFileKey())
                .isEqualTo("courses/subtitles/ai/lesson-1/en.words.json");
        assertThat(completedJob.getStatus()).isEqualTo(SubtitleJobStatus.SUCCEEDED);
        assertThat(completedJob.getOutputSubtitleId()).isEqualTo("subtitle-1");

        subtitleJobService.markCompleted(new SubtitleJobCompletedRequest(
                "job-2",
                completedJob.getExpectedOutputFileKey(),
                null,
                "job-2.vtt",
                "text/vtt",
                2048L,
                "en",
                null,
                null,
                null
        ));
        verify(subtitleTrackRepository, times(1)).save(any(SubtitleTrack.class));
    }

    private void authenticate(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );
    }

    private VideoLesson videoLesson(String lessonId) {
        VideoLesson videoLesson = new VideoLesson();
        videoLesson.setLessonId(lessonId);
        videoLesson.setFileKey("videos/" + lessonId + ".mp4");
        videoLesson.setVideoUrl("https://cdn.example.com/videos/" + lessonId + ".mp4");
        return videoLesson;
    }

    private SubtitleTrack subtitle(String id, String languageCode, boolean isDefault) {
        SubtitleTrack subtitle = new SubtitleTrack();
        subtitle.setId(id);
        subtitle.setLanguageCode(languageCode);
        subtitle.setDisplayName(languageCode.toUpperCase());
        subtitle.setFileKey("courses/subtitles/lesson-1/" + languageCode + "/source.vtt");
        subtitle.setFileUrl("https://cdn.example.com/" + subtitle.getFileKey());
        subtitle.setFileType("text/vtt");
        subtitle.setFileSize(1024L);
        subtitle.setFormat(SubtitleFormat.VTT);
        subtitle.setSource(SubtitleSource.MANUAL);
        subtitle.setStatus(SubtitleStatus.READY);
        subtitle.setIsDefault(isDefault);
        subtitle.setVideoLesson(videoLesson("lesson-1"));
        return subtitle;
    }

    private SubtitleJob job(VideoLesson videoLesson, SubtitleJobType type, String targetLanguageCode) {
        SubtitleJob job = new SubtitleJob();
        job.setId(type == SubtitleJobType.TRANSLATE ? "job-2" : "job-1");
        job.setJobType(type);
        job.setStatus(SubtitleJobStatus.QUEUED);
        job.setSourceSubtitleId("source-1");
        job.setSourceLanguageCode("vi");
        job.setTargetLanguageCode(targetLanguageCode);
        job.setDisplayName(targetLanguageCode.toUpperCase());
        job.setExpectedOutputFileKey("courses/subtitles/ai/" + videoLesson.getLessonId() + "/" + targetLanguageCode + ".vtt");
        job.setProgressPercent(0);
        job.setVideoLesson(videoLesson);
        return job;
    }

    private SubtitleJob savedJob(SubtitleJob job, String id) {
        if (job.getId() == null) {
            job.setId(id);
        }
        return job;
    }

    private SubtitleJobResponse response(SubtitleJob job) {
        return new SubtitleJobResponse(
                job.getId(),
                job.getJobType(),
                job.getStatus(),
                job.getSourceSubtitleId(),
                job.getOutputSubtitleId(),
                job.getSourceLanguageCode(),
                job.getTargetLanguageCode(),
                job.getDisplayName(),
                job.getExpectedOutputFileKey(),
                job.getProgressPercent(),
                job.getErrorCode(),
                job.getErrorMessage()
        );
    }
}
