package com.cinx.course.service.subtitle;

import com.cinx.common.dto.PresignedUrlResponse;
import com.cinx.course.dto.request.CreateSubtitleTrackRequest;
import com.cinx.course.dto.request.UpdateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleTrackResponse;

import java.util.List;

public interface ISubtitleTrackService {
    List<SubtitleTrackResponse> getSubtitlesByLessonId(String lessonId);

    PresignedUrlResponse getSubtitlePresignedUrl(String lessonId, String fileName, String contentType, String languageCode);

    SubtitleTrackResponse createSubtitle(String lessonId, CreateSubtitleTrackRequest request);

    SubtitleTrackResponse updateSubtitle(String lessonId, String subtitleId, UpdateSubtitleTrackRequest request);

    void deleteSubtitle(String lessonId, String subtitleId);
}
