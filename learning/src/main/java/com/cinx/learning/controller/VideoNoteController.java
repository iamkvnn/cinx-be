package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.CreateVideoNoteRequest;
import com.cinx.learning.dto.request.UpdateVideoNoteRequest;
import com.cinx.learning.dto.response.VideoNoteDto;
import com.cinx.learning.service.IVideoNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/video-notes")
@RequiredArgsConstructor
public class VideoNoteController {
    private final IVideoNoteService videoNoteService;

    @PostMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<VideoNoteDto>> createNote(@Valid @RequestBody CreateVideoNoteRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Video note created successfully", videoNoteService.createNote(userId, request)));
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<VideoNoteDto>>> getNotesByLesson(@PathVariable String lessonId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Video notes fetched successfully", videoNoteService.getNotesByLesson(userId, lessonId)));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<VideoNoteDto>>> getNotesByCourse(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Video notes fetched successfully", videoNoteService.getNotesByCourse(userId, courseId)));
    }

    @PutMapping("/{noteId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<VideoNoteDto>> updateNote(@PathVariable String noteId, @Valid @RequestBody UpdateVideoNoteRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Video note updated successfully", videoNoteService.updateNote(userId, noteId, request)));
    }

    @DeleteMapping("/{noteId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable String noteId) {
        String userId = AuthenticationUtil.extractUserId();
        videoNoteService.deleteNote(userId, noteId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Video note deleted successfully", null));
    }
}
