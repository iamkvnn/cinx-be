package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.service.lesson.ILessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections/{sectionId}/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final ILessonService lessonService;
    private final LessonMapper lessonMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessonsBySectionId(@PathVariable String sectionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Success",
                lessonService.getLessonsBySectionId(sectionId).stream().map(lessonMapper::toDto).toList()
        ));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonById(
            @PathVariable String sectionId,
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Success",
                lessonMapper.toDto(lessonService.getLessonById(sectionId, lessonId))
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @PathVariable String sectionId,
            @RequestBody CreateLessonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lesson created successfully",
                lessonMapper.toDto(lessonService.createLesson(sectionId, request))
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable String sectionId,
            @PathVariable String lessonId,
            @RequestBody UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lesson updated successfully",
                lessonMapper.toDto(lessonService.updateLesson(sectionId, lessonId, request))
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(
            @PathVariable String sectionId,
            @PathVariable String lessonId
    ) {
        lessonService.deleteLesson(sectionId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully", null));
    }
}
