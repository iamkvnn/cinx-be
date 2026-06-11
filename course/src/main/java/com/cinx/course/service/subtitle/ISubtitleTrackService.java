package com.cinx.course.service.subtitle;

import com.cinx.common.dto.PresignedUrlResponse;
import com.cinx.course.dto.request.CreateSubtitleTrackRequest;
import com.cinx.course.dto.request.UpdateSubtitleContentRequest;
import com.cinx.course.dto.request.UpdateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleContentResponse;
import com.cinx.course.dto.response.SubtitleTrackResponse;
import com.cinx.course.dto.response.SubtitleWordConfidenceResponse;

import java.util.List;

public interface ISubtitleTrackService {
    List<SubtitleTrackResponse> getSubtitlesByLessonId(String lessonId);

    PresignedUrlResponse getSubtitlePresignedUrl(String lessonId, String fileName, String contentType, String languageCode);

    SubtitleTrackResponse createSubtitle(String lessonId, CreateSubtitleTrackRequest request);

    SubtitleTrackResponse updateSubtitle(String lessonId, String subtitleId, UpdateSubtitleTrackRequest request);

    SubtitleContentResponse getSubtitleContent(String lessonId, String subtitleId);

    SubtitleTrackResponse updateSubtitleContent(String lessonId, String subtitleId, UpdateSubtitleContentRequest request);

    SubtitleWordConfidenceResponse getSubtitleWordConfidence(String lessonId, String subtitleId);

    void deleteSubtitle(String lessonId, String subtitleId);
}
