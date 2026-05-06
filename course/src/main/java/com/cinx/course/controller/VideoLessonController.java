package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.request.UpdateVideoLessonRequest;
import com.cinx.course.dto.response.VideoLessonResponse;
import com.cinx.course.service.video.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/videos")
@RequiredArgsConstructor
public class VideoLessonController {
    private final IVideoService videoService;

    @GetMapping
    public ResponseEntity<ApiResponse<VideoLessonResponse>> getVideoByLessonId(@PathVariable String lessonId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", videoService.getVideoByLessonId(lessonId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVideoLesson(@PathVariable String lessonId, @RequestBody CreateVideoLessonRequest request) {
        videoService.createVideo(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateVideoLesson(@PathVariable String lessonId, @RequestBody UpdateVideoLessonRequest request) {
        videoService.updateVideo(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteVideoLesson(@PathVariable String lessonId) {
        videoService.deleteVideo(lessonId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
