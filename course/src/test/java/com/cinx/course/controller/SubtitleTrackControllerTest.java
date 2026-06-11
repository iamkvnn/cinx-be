package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.consts.SubtitleJobStatus;
import com.cinx.course.consts.SubtitleJobType;
import com.cinx.course.dto.request.GenerateDefaultSubtitleJobRequest;
import com.cinx.course.dto.request.TranslateSubtitleJobRequest;
import com.cinx.course.dto.response.SubtitleJobResponse;
import com.cinx.course.service.subtitle.ISubtitleJobService;
import com.cinx.course.service.subtitle.ISubtitleTrackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubtitleTrackControllerTest {
    @Mock
    private ISubtitleTrackService subtitleTrackService;
    @Mock
    private ISubtitleJobService subtitleJobService;

    @Test
    void createDefaultSubtitleJobWrapsApiResponse() {
        SubtitleTrackController controller = new SubtitleTrackController(subtitleTrackService, subtitleJobService);
        GenerateDefaultSubtitleJobRequest request = new GenerateDefaultSubtitleJobRequest("vi", null);
        SubtitleJobResponse job = job("job-1", SubtitleJobType.GENERATE_DEFAULT, "vi");
        when(subtitleJobService.createDefaultSubtitleJob("course-1", "lesson-1", request)).thenReturn(job);

        ResponseEntity<ApiResponse<SubtitleJobResponse>> response = controller.createDefaultSubtitleJob(
                "course-1",
                "lesson-1",
                request
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(job);
        verify(subtitleJobService).createDefaultSubtitleJob("course-1", "lesson-1", request);
    }

    @Test
    void createTranslationJobsWrapsApiResponse() {
        SubtitleTrackController controller = new SubtitleTrackController(subtitleTrackService, subtitleJobService);
        TranslateSubtitleJobRequest request = new TranslateSubtitleJobRequest(null, List.of("en"));
        List<SubtitleJobResponse> jobs = List.of(job("job-1", SubtitleJobType.TRANSLATE, "en"));
        when(subtitleJobService.createTranslationJobs("course-1", "lesson-1", request)).thenReturn(jobs);

        ResponseEntity<ApiResponse<List<SubtitleJobResponse>>> response = controller.createTranslationJobs(
                "course-1",
                "lesson-1",
                request
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(jobs);
        verify(subtitleJobService).createTranslationJobs("course-1", "lesson-1", request);
    }

    @Test
    void getSubtitleJobWrapsApiResponse() {
        SubtitleTrackController controller = new SubtitleTrackController(subtitleTrackService, subtitleJobService);
        SubtitleJobResponse job = job("job-1", SubtitleJobType.TRANSLATE, "en");
        when(subtitleJobService.getJobById("course-1", "lesson-1", "job-1")).thenReturn(job);

        ResponseEntity<ApiResponse<SubtitleJobResponse>> response = controller.getSubtitleJob(
                "course-1",
                "lesson-1",
                "job-1"
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(job);
        verify(subtitleJobService).getJobById("course-1", "lesson-1", "job-1");
    }

    private SubtitleJobResponse job(String id, SubtitleJobType type, String targetLanguageCode) {
        return new SubtitleJobResponse(
                id,
                type,
                SubtitleJobStatus.QUEUED,
                null,
                null,
                null,
                targetLanguageCode,
                targetLanguageCode.toUpperCase(),
                "courses/subtitles/ai/lesson-1/" + targetLanguageCode + "/" + id + ".vtt",
                0,
                null,
                null
        );
    }
}
