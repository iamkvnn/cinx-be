package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.response.QuizSessionQuestionResponse;
import com.cinx.learning.dto.response.QuizSessionResponse;
import com.cinx.learning.service.quiz.IQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/quiz-sessions")
public class QuizSessionController {
    private final IQuizService quizService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<QuizSessionResponse>> getQuizSessions(
            @RequestParam(required = false) String userId,
            @RequestParam String quizLessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getQuizSessions(userId, quizLessonId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{quizSessionId}")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> getQuizSession(@PathVariable String quizSessionId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", quizService.getQuizSession(quizSessionId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{quizSessionId}/questions")
    public ResponseEntity<PaginatedApiResponse<QuizSessionQuestionResponse>> getQuizSessionQuestions(
            @PathVariable String quizSessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getQuizSessionQuestions(quizSessionId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<QuizSessionResponse>> createQuizSession(
            @RequestParam String quizLessonId
    ) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz session created successfully", quizService.createQuizSession(userId, quizLessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{quizSessionId}/choose")
    public ResponseEntity<ApiResponse<?>> chooseQuizSessionQuestion(
            @PathVariable String quizSessionId,
            @RequestBody ChooseQuizAnswerRequest request
    ) {
        quizService.chooseQuizSessionQuestion(quizSessionId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer chosen successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{quizSessionId}/submit")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> submitQuizSession(
            @PathVariable String quizSessionId,
            @RequestBody SubmitQuizSessionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz session submitted successfully", quizService.submitQuizSession(quizSessionId, request)));
    }
}
