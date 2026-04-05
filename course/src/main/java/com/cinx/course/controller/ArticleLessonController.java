package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;
import com.cinx.course.service.article.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article-lessons")
@RequiredArgsConstructor
public class ArticleLessonController {
    private final IArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<ArticleLessonResponse>> getArticleByLessonId(@RequestParam String lessonId) {
        return ResponseEntity.ok(ApiResponse.success("Success", articleService.getArticleByLessonId(lessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createArticleLesson(@RequestParam String lessonId, @RequestBody CreateArticleLessonRequest request) {
        articleService.createArticle(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateArticleLesson(@RequestParam String lessonId, @RequestBody CreateArticleLessonRequest request) {
        articleService.updateArticle(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteArticleLesson(@RequestParam String lessonId) {
        articleService.deleteArticle(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }
}
