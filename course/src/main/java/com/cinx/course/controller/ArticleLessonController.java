package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.service.article.IArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article-lessons")
@RequiredArgsConstructor
public class ArticleLessonController {
    private final IArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getArticleByLessonId(@RequestParam String lessonId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", articleService.getArticleByLessonId(lessonId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createArticleLesson(@RequestParam String lessonId, @RequestBody CreateArticleLessonRequest request) {
        articleService.createArticle(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateArticleLesson(@RequestParam String lessonId, @RequestBody CreateArticleLessonRequest request) {
        articleService.updateArticle(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteArticleLesson(@RequestParam String lessonId) {
        articleService.deleteArticle(lessonId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
