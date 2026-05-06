package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateQuizLessonRequest;
import com.cinx.course.dto.request.UpdateQuizLessonRequest;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.service.quiz.IQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/quizzes")
@RequiredArgsConstructor
public class QuizLessonController {
    private final IQuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<QuizLessonResponse>> getQuizByLessonId(@PathVariable String lessonId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", quizService.getQuizByLessonId(lessonId))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createQuizLesson(@PathVariable String lessonId, @RequestBody CreateQuizLessonRequest request) {
        quizService.createQuiz(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateQuizLesson(@PathVariable String lessonId, @RequestBody UpdateQuizLessonRequest request) {
        quizService.updateQuiz(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteQuizLesson(@PathVariable String lessonId) {
        quizService.deleteQuiz(lessonId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
