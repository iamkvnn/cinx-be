package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateVideoQuestionRequest;
import com.cinx.course.dto.request.UpdateVideoQuestionRequest;
import com.cinx.course.dto.response.VideoQuestionResponse;
import com.cinx.course.service.videoquestion.IVideoQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons/{lessonId}/videos/questions")
@RequiredArgsConstructor
public class VideoQuestionController {

    private final IVideoQuestionService videoQuestionService;

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<VideoQuestionResponse>>> getQuestionsByLessonId(
            @PathVariable String courseId,
            @PathVariable String lessonId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Questions fetched successfully",
                videoQuestionService.getQuestionsByLessonId(lessonId)));
    }

    @GetMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<VideoQuestionResponse>> getQuestionById(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String id
    ) {
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Question fetched successfully", videoQuestionService.getQuestionById(lessonId, id)));
    }

    @PostMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<VideoQuestionResponse>> createQuestion(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody @Valid CreateVideoQuestionRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Video question created",
                videoQuestionService.createQuestion(lessonId, request)));
    }

    @PutMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<VideoQuestionResponse>> updateQuestion(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String id,
            @RequestBody @Valid UpdateVideoQuestionRequest request) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Video question updated", videoQuestionService.updateQuestion(lessonId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @PathVariable String id
    ) {
        videoQuestionService.deleteQuestion(lessonId, id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Video question deleted", null));
    }
}
