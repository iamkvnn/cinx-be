package com.cinx.course.dto.response;

import com.cinx.course.consts.SubtitleFormat;
import com.cinx.course.consts.SubtitleSource;
import com.cinx.course.consts.SubtitleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record SubtitleTrackResponse(
        @Schema(example = "sub_123")
        String id,
        @Schema(example = "vi")
        String languageCode,
        @Schema(example = "Vietnamese")
        String displayName,
        @Schema(example = "https://cdn.example.com/courses/subtitles/lesson-1/vi/sub_123.vtt")
        String fileUrl,
        @Schema(example = "courses/subtitles/lesson-1/vi/sub_123.vtt")
        String fileKey,
        @Schema(example = "courses/subtitles/ai/lesson-1/vi/job-123.words.json")
        String wordConfidenceFileKey,
        @Schema(example = "https://cdn.example.com/courses/subtitles/ai/lesson-1/vi/job-123.words.json")
        String wordConfidenceFileUrl,
        @Schema(example = "intro.vi.srt")
        String fileName,
        @Schema(example = "text/vtt")
        String fileType,
        @Schema(example = "4096")
        Long fileSize,
        @Schema(example = "VTT")
        SubtitleFormat format,
        @Schema(example = "MANUAL")
        SubtitleSource source,
        @Schema(example = "READY")
        SubtitleStatus status,
        @Schema(example = "true")
        Boolean isDefault
) {
}
