package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.dto.response.VideoLessonTrackingHistoryResponse;
import com.cinx.learning.service.video.IVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PaginationWrapper.wrap(videoService.getVideoLessonTrackingHistories(videoLessonId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<VideoLessonTrackingHistoryResponse>> getVideoLessonTrackingHistory(@RequestParam String videoLessonId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", videoService.getVideoLessonTrackingHistory(userId, videoLessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> trackVideoProgress(
            @RequestBody TrackingVideoLessonRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        videoService.trackVideoProgress(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Video progress tracked successfully", null));
    }
}
