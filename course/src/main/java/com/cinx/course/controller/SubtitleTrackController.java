package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PresignedUrlResponse;
import com.cinx.course.dto.request.CreateSubtitleTrackRequest;
import com.cinx.course.dto.request.GenerateDefaultSubtitleJobRequest;
import com.cinx.course.dto.request.TranslateSubtitleJobRequest;
import com.cinx.course.dto.request.UpdateSubtitleContentRequest;
import com.cinx.course.dto.request.UpdateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleContentResponse;
import com.cinx.course.dto.response.SubtitleJobResponse;
import com.cinx.course.dto.response.SubtitleTrackResponse;
import com.cinx.course.dto.response.SubtitleWordConfidenceResponse;
import com.cinx.course.service.subtitle.ISubtitleJobService;
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
    private final ISubtitleJobService subtitleJobService;

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

    @PostMapping("/ai/default")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleJobResponse>> createDefaultSubtitleJob(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody @Valid GenerateDefaultSubtitleJobRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Default subtitle generation job created successfully",
                subtitleJobService.createDefaultSubtitleJob(courseId, lessonId, request)
        ));
    }

    @PostMapping("/ai/translations")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<SubtitleJobResponse>>> createTranslationJobs(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody @Valid TranslateSubtitleJobRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle translation jobs created successfully",
                subtitleJobService.createTranslationJobs(courseId, lessonId, request)
        ));
    }

    @GetMapping("/jobs")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<SubtitleJobResponse>>> getSubtitleJobs(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle jobs fetched successfully",
                subtitleJobService.getJobsByLessonId(courseId, lessonId)
        ));
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleJobResponse>> getSubtitleJob(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String jobId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle job fetched successfully",
                subtitleJobService.getJobById(courseId, lessonId, jobId)
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

    @GetMapping("/{subtitleId}/content")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleContentResponse>> getSubtitleContent(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String subtitleId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle content fetched successfully",
                subtitleTrackService.getSubtitleContent(lessonId, subtitleId)
        ));
    }

    @PutMapping("/{subtitleId}/content")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleTrackResponse>> updateSubtitleContent(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String subtitleId,
            @RequestBody @Valid UpdateSubtitleContentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle content updated successfully",
                subtitleTrackService.updateSubtitleContent(lessonId, subtitleId, request)
        ));
    }

    @GetMapping("/{subtitleId}/word-confidence")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<SubtitleWordConfidenceResponse>> getSubtitleWordConfidence(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String subtitleId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Subtitle word confidence fetched successfully",
                subtitleTrackService.getSubtitleWordConfidence(lessonId, subtitleId)
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
