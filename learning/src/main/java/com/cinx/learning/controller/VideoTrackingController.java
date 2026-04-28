package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.SubmitVideoQuestionRequest;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.response.InVideoAssessmentSubmissionResponse;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import com.cinx.learning.service.video.IVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/video-tracking")
public class VideoTrackingController {
    private final IVideoService videoService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<VideoLessonTrackingHistoryResponse>> getVideoLessonTrackingHistories(
            @RequestParam String videoLessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(PaginationWrapper.wrap(videoService.getVideoLessonTrackingHistories(videoLessonId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<VideoLessonTrackingHistoryResponse>> getVideoLessonTrackingHistory(@RequestParam String videoLessonId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", videoService.getVideoLessonTrackingHistory(userId, videoLessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/video-lessons/{videoLessonId}/submissions")
    public ResponseEntity<ApiResponse<List<InVideoAssessmentSubmissionResponse>>> getVideoQuestionSubmissions(
            @PathVariable String videoLessonId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Submissions fetched successfully", videoService.getVideoQuestionSubmissions(userId, videoLessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> trackVideoProgress(
            @RequestBody TrackingVideoLessonRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        videoService.trackVideoProgress(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Video progress tracked successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/questions/submit")
    public ResponseEntity<ApiResponse<?>> submitVideoQuestionAnswer(
            @RequestBody SubmitVideoQuestionRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        videoService.submitVideoQuestionAnswer(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer submitted successfully", null));
    }
}
