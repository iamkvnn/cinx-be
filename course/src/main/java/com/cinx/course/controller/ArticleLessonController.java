package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;
import com.cinx.course.service.article.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/articles")
@RequiredArgsConstructor
public class ArticleLessonController {
    private final IArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<ArticleLessonResponse>> getArticleByLessonId(@PathVariable String lessonId) {
        return ResponseEntity.ok(ApiResponse.success("Success", articleService.getArticleByLessonId(lessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createArticleLesson(@PathVariable String lessonId, @RequestBody CreateArticleLessonRequest request) {
        articleService.createArticle(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateArticleLesson(@PathVariable String lessonId, @RequestBody UpdateArticleLessonRequest request) {
        articleService.updateArticle(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }
}
