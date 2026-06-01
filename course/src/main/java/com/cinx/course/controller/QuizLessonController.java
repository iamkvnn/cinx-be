package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateQuizLessonRequest;
import com.cinx.course.dto.request.SyncQuizRequest;
import com.cinx.course.dto.request.UpdateQuizLessonRequest;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.service.quiz.IQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons/{lessonId}/quizzes")
@RequiredArgsConstructor
public class QuizLessonController {
    private final IQuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<QuizLessonResponse>> getQuizByLessonId(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", quizService.getQuizByLessonId(courseId, lessonId)));
    }

    @Operation(summary = "Create quiz with initial questions", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createQuizLesson(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @Valid @RequestBody CreateQuizLessonRequest request
    ) {
        quizService.createQuiz(courseId, lessonId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz created successfully", null));
    }

    @Operation(summary = "Update quiz (no questions)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateQuizSettings(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @Valid @RequestBody UpdateQuizLessonRequest request
    ) {
        quizService.updateQuiz(courseId, lessonId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz updated successfully", null));
    }

    @Operation(summary = "Sync quiz changes to learning service (with optional regrade trigger)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<?>> syncQuiz(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @Valid @RequestBody SyncQuizRequest request
    ) {
        quizService.syncQuiz(courseId, lessonId, request);
        return ResponseEntity.ok(new ApiResponse<>(true,
                Boolean.TRUE.equals(request.triggerRegrade())
                        ? "Quiz synced and regrade triggered"
                        : "Quiz sync acknowledged (no regrade)",
                null));
    }
}
