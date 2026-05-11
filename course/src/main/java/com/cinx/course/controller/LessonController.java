package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonPreviewResponse;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.model.Lesson;
import com.cinx.course.service.article.IArticleService;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.video.IVideoService;
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
    private final IVideoService videoService;
    private final IArticleService articleService;
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

    @GetMapping("/{lessonId}/preview")
    public ResponseEntity<ApiResponse<LessonPreviewResponse>> getLessonPreview(
            @PathVariable String sectionId,
            @PathVariable String lessonId
    ) {
        Lesson lesson = lessonService.getLessonById(sectionId, lessonId);
        if (lesson.getIsPreview() == null || !lesson.getIsPreview()) {
            throw new com.cinx.common.exception.BadRequestException("This lesson is not available for preview");
        }

        Object content = null;
        if (lesson.getLessonType() == com.cinx.course.consts.LessonType.VIDEO) {
            content = videoService.getVideoByLessonId(lessonId);
        } else if (lesson.getLessonType() == com.cinx.course.consts.LessonType.ARTICLE) {
            content = articleService.getArticleByLessonId(lessonId);
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Success",
                new LessonPreviewResponse(lessonMapper.toDto(lesson), content)
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
