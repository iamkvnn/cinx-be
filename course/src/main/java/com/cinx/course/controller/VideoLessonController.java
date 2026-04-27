package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.request.UpdateVideoLessonRequest;
import com.cinx.course.service.video.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/video-lessons")
@RequiredArgsConstructor
public class VideoLessonController {
    private final IVideoService videoService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVideoLesson(@RequestParam String lessonId, @RequestBody CreateVideoLessonRequest request) {
        videoService.createVideo(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateVideoLesson(@RequestParam String lessonId, @RequestBody UpdateVideoLessonRequest request) {
        videoService.updateVideo(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteVideoLesson(@RequestParam String lessonId) {
        videoService.deleteVideo(lessonId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
