package com.cinx.course.service.subtitle;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.course.consts.SubtitleFormat;
import com.cinx.course.consts.SubtitleStatus;
import com.cinx.course.dto.request.CreateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleTrackResponse;
import com.cinx.course.mapper.SubtitleTrackMapper;
import com.cinx.course.model.SubtitleTrack;
import com.cinx.course.model.VideoLesson;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubtitleTrackServiceTest {
    @Mock
    private SubtitleTrackRepository subtitleTrackRepository;
    @Mock
    private VideoLessonRepository videoLessonRepository;
    @Mock
    private SubtitleTrackMapper subtitleTrackMapper;
    @Spy
    private SubtitleFileProcessor subtitleFileProcessor;
    @Mock
    private S3Service s3Service;
    @Mock
    private ILessonService lessonService;

    @InjectMocks
    private SubtitleTrackService subtitleTrackService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSubtitle_convertsSrtAndMakesFirstSubtitleDefault() {
        authenticate("instructor-1");
        VideoLesson videoLesson = videoLesson("lesson-1");
        when(videoLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(videoLesson));
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode("lesson-1", "vi")).thenReturn(Optional.empty());
        when(subtitleTrackRepository.countByVideoLessonLessonId("lesson-1")).thenReturn(0L);
        when(s3Service.readTextObject("uploads/intro.vi.srt")).thenReturn("""
                1
                00:00:01,000 --> 00:00:03,000
                Xin chao
                """);
        when(s3Service.publicUrl(any())).thenAnswer(invocation -> "https://cdn.example.com/" + invocation.getArgument(0));
        when(subtitleTrackRepository.save(any(SubtitleTrack.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(subtitleTrackMapper.toDto(any(SubtitleTrack.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        SubtitleTrackResponse response = subtitleTrackService.createSubtitle("instructor-1", "course-1", "lesson-1", new CreateSubtitleTrackRequest(
                "vi",
                "Vietnamese",
                "uploads/intro.vi.srt",
                "intro.vi.srt",
                "application/x-subrip",
                1024L,
                false
        ));

        ArgumentCaptor<String> uploadedContent = ArgumentCaptor.forClass(String.class);
        verify(s3Service).uploadTextFile(any(), uploadedContent.capture(), org.mockito.ArgumentMatchers.eq("text/vtt"));
        assertThat(uploadedContent.getValue()).startsWith("WEBVTT");
        assertThat(uploadedContent.getValue()).contains("00:00:01.000 --> 00:00:03.000");
        assertThat(response.isDefault()).isTrue();
        assertThat(response.format()).isEqualTo(SubtitleFormat.VTT);
        assertThat(response.status()).isEqualTo(SubtitleStatus.READY);
    }

    @Test
    void createSubtitle_rejectsDuplicateLanguage() {
        authenticate("instructor-1");
        when(videoLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(videoLesson("lesson-1")));
        when(subtitleTrackRepository.findByVideoLessonLessonIdAndLanguageCode("lesson-1", "en"))
                .thenReturn(Optional.of(new SubtitleTrack()));

        assertThatThrownBy(() -> subtitleTrackService.createSubtitle("instructor-1", "course-1", "lesson-1", new CreateSubtitleTrackRequest(
                "en",
                "English",
                "uploads/intro.en.vtt",
                "intro.en.vtt",
                "text/vtt",
                1024L,
                true
        ))).isInstanceOf(AlreadyExistException.class);

        verify(s3Service, never()).readTextObject(any());
        verify(subtitleTrackRepository, never()).save(any());
    }

    private void authenticate(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );
    }

    private VideoLesson videoLesson(String lessonId) {
        VideoLesson videoLesson = new VideoLesson();
        videoLesson.setLessonId(lessonId);
        videoLesson.setDuration(600);
        return videoLesson;
    }

    private SubtitleTrackResponse response(SubtitleTrack track) {
        return new SubtitleTrackResponse(
                track.getId(),
                track.getLanguageCode(),
                track.getDisplayName(),
                track.getFileUrl(),
                track.getFileKey(),
                track.getWordConfidenceFileKey(),
                track.getWordConfidenceFileUrl(),
                track.getFileName(),
                track.getFileType(),
                track.getFileSize(),
                track.getFormat(),
                track.getSource(),
                track.getStatus(),
                track.getIsDefault()
        );
    }
}
