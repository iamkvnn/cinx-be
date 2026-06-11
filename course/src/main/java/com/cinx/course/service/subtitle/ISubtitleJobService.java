package com.cinx.course.service.subtitle;

import com.cinx.course.dto.request.GenerateDefaultSubtitleJobRequest;
import com.cinx.course.dto.request.SubtitleJobCompletedRequest;
import com.cinx.course.dto.request.TranslateSubtitleJobRequest;
import com.cinx.course.dto.response.SubtitleJobResponse;

import java.util.List;

public interface ISubtitleJobService {
    SubtitleJobResponse createDefaultSubtitleJob(String courseId, String lessonId, GenerateDefaultSubtitleJobRequest request);

    List<SubtitleJobResponse> createTranslationJobs(String courseId, String lessonId, TranslateSubtitleJobRequest request);

    List<SubtitleJobResponse> getJobsByLessonId(String courseId, String lessonId);

    SubtitleJobResponse getJobById(String courseId, String lessonId, String jobId);

    void markProcessing(String jobId, Integer progressPercent);

    void markCompleted(SubtitleJobCompletedRequest request);

    void markFailed(String jobId, String errorCode, String errorMessage);
}
