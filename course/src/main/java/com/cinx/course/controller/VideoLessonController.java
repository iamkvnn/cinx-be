package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.request.UpdateVideoLessonRequest;
import com.cinx.course.dto.response.VideoLessonResponse;
import com.cinx.course.service.video.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons/{lessonId}/videos")
@RequiredArgsConstructor
public class VideoLessonController {
    private final IVideoService videoService;

    @GetMapping
    public ResponseEntity<ApiResponse<VideoLessonResponse>> getVideoByLessonId(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", videoService.getReadableVideoByLessonId(currentUserId, courseId, lessonId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVideoLesson(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody CreateVideoLessonRequest request
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        videoService.createVideo(currentUserId, courseId, lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateVideoLesson(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody UpdateVideoLessonRequest request
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        videoService.updateVideo(currentUserId, courseId, lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
