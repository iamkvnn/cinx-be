package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.service.quiz.IQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/quiz-sessions")
public class QuizSessionController {
    private final IQuizService quizService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getQuizSessions(
            @RequestParam(required = false) String userId,
            @RequestParam String quizLessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getQuizSessions(userId, quizLessonId, page, size)));
    }

    @GetMapping("/{quizSessionId}")
    public ResponseEntity<ApiResponse<?>> getQuizSession(@PathVariable String quizSessionId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", quizService.getQuizSession(quizSessionId)));
    }

    @GetMapping("/{quizSessionId}/questions")
    public ResponseEntity<PaginatedApiResponse<?>> getQuizSessionQuestions(
            @PathVariable String quizSessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getQuizSessionQuestions(quizSessionId, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createQuizSession(
            @RequestParam String quizLessonId
    ) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz session created successfully", quizService.createQuizSession(userId, quizLessonId)));
    }

    @PostMapping("/{quizSessionId}/choose")
    public ResponseEntity<ApiResponse<?>> chooseQuizSessionQuestion(
            @PathVariable String quizSessionId,
            @RequestBody ChooseQuizAnswerRequest request
    ) {
        quizService.chooseQuizSessionQuestion(quizSessionId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer chosen successfully", null));
    }

    @PostMapping("/{quizSessionId}/submit")
    public ResponseEntity<ApiResponse<?>> submitQuizSession(
            @PathVariable String quizSessionId,
            @RequestBody SubmitQuizSessionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz session submitted successfully", quizService.submitQuizSession(quizSessionId, request)));
    }
}
