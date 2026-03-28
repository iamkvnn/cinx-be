package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.TrackingVideoLessonRequest;
import com.cinx.learning.service.video.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/video-tracking")
public class VideoTrackingController {
    private final IVideoService videoService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getVideoLessonTrackingHistories(
            @RequestParam String videoLessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PaginationWrapper.wrap(videoService.getVideoLessonTrackingHistories(videoLessonId, page, size)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getVideoLessonTrackingHistory(@RequestParam String videoLessonId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", videoService.getVideoLessonTrackingHistory(userId, videoLessonId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> trackVideoProgress(
            @RequestBody TrackingVideoLessonRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        videoService.trackVideoProgress(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Video progress tracked successfully", null));
    }
}
