package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.service.lesson.ILessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/sections/{sectionId}/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final ILessonService lessonService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @PathVariable String courseId,
            @PathVariable String sectionId,
            @RequestBody CreateLessonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lesson created successfully",
                lessonService.createLesson(courseId, sectionId, request)
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable String courseId,
            @PathVariable String sectionId,
            @PathVariable String lessonId,
            @RequestBody UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lesson updated successfully",
                lessonService.updateLesson(courseId, sectionId, lessonId, request)
        ));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(
            @PathVariable String courseId,
            @PathVariable String sectionId,
            @PathVariable String lessonId
    ) {
        lessonService.deleteLesson(courseId, sectionId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson deleted successfully", null));
    }
}
