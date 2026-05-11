package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.request.CreateQuizQuestionRequest;
import com.cinx.course.dto.request.UpdateQuizQuestionRequest;
import com.cinx.course.dto.response.QuizQuestionResponse;
import com.cinx.course.service.quiz.IQuizQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/quizzes/questions")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final IQuizQuestionService quizQuestionService;

    @Operation(summary = "List all questions for a quiz", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizQuestionResponse>>> getQuestions(@PathVariable String lessonId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", quizQuestionService.getQuestions(lessonId)));
    }

    @Operation(summary = "Add a question to a quiz", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<QuizQuestionResponse>> addQuestion(
            @PathVariable String lessonId,
            @Valid @RequestBody CreateQuizQuestionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Question added successfully", quizQuestionService.addQuestion(lessonId, request)));
    }

    @Operation(summary = "Update a question (options use merge strategy: id→update, no id→create, missing→delete)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuizQuestionResponse>> updateQuestion(
            @PathVariable String lessonId,
            @PathVariable String questionId,
            @Valid @RequestBody UpdateQuizQuestionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Question updated successfully", quizQuestionService.updateQuestion(lessonId, questionId, request)));
    }

    @Operation(summary = "Delete a question (blocked if it would violate numberOfQuestionPerQuizSession)",
               security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<?>> deleteQuestion(
            @PathVariable String lessonId,
            @PathVariable String questionId
    ) {
        quizQuestionService.deleteQuestion(lessonId, questionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question deleted successfully", null));
    }
}
