package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PresignedUrlResponse;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateSubtitleTrackRequest;
import com.cinx.course.dto.request.UpdateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleTrackResponse;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.subtitle.ISubtitleTrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons/{lessonId}/videos/subtitles")
@RequiredArgsConstructor
public class SubtitleTrackController {
    private final ISubtitleTrackService subtitleTrackService;

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<SubtitleTrackResponse>>> getSubtitles(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitles fetched successfully",
                subtitleTrackService.getSubtitlesByLessonId(lessonId)
        ));
    }

    @GetMapping("/presigned-url")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getSubtitlePresignedUrl(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam String languageCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle upload URL generated successfully",
                subtitleTrackService.getSubtitlePresignedUrl(lessonId, fileName, contentType, languageCode)
        ));
    }

    @PostMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleTrackResponse>> createSubtitle(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody @Valid CreateSubtitleTrackRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle created successfully",
                subtitleTrackService.createSubtitle(lessonId, request)
        ));
    }

    @PutMapping("/{subtitleId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleTrackResponse>> updateSubtitle(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String subtitleId,
            @RequestBody @Valid UpdateSubtitleTrackRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle updated successfully",
                subtitleTrackService.updateSubtitle(lessonId, subtitleId, request)
        ));
    }

    @DeleteMapping("/{subtitleId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> deleteSubtitle(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String subtitleId
    ) {
        subtitleTrackService.deleteSubtitle(lessonId, subtitleId);
        return ResponseEntity.ok(ApiResponse.success("Subtitle deleted successfully", null));
    }
}
