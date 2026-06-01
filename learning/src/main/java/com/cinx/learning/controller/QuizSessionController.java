package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.GradeEssayRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.response.QuizQuestionAnalyticsResponse;
import com.cinx.learning.dto.response.QuizSessionQuestionResponse;
import com.cinx.learning.dto.response.QuizSessionResponse;
import com.cinx.learning.service.quiz.IQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning")
public class QuizSessionController {
    private final IQuizService quizService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/lessons/{lessonId}/quiz-sessions")
    public ResponseEntity<PaginatedApiResponse<QuizSessionResponse>> getQuizSessions(
            @PathVariable String lessonId,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getQuizSessions(userId, lessonId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/quiz-sessions/{quizSessionId}")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> getQuizSession(@PathVariable String quizSessionId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", quizService.getQuizSession(quizSessionId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/quiz-sessions/{quizSessionId}/questions")
    public ResponseEntity<PaginatedApiResponse<QuizSessionQuestionResponse>> getQuizSessionQuestions(
            @PathVariable String quizSessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getQuizSessionQuestions(quizSessionId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/quiz-sessions")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> createQuizSession(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz session created successfully", quizService.createQuizSession(courseId, userId, lessonId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/quiz-sessions/{quizSessionId}/choose")
    public ResponseEntity<ApiResponse<?>> chooseQuizSessionQuestion(
            @PathVariable String quizSessionId,
            @RequestBody ChooseQuizAnswerRequest request
    ) {
        quizService.chooseQuizSessionQuestion(quizSessionId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer chosen successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/quiz-sessions/{quizSessionId}/submit")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> submitQuizSession(
            @PathVariable String quizSessionId,
            @RequestBody SubmitQuizSessionRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz session submitted successfully", quizService.submitQuizSession(quizSessionId, request)));
    }

    @Operation(summary = "Get analytical statistics for a quiz", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/courses/{courseId}/lessons/{lessonId}/quiz-sessions/analytics")
    public ResponseEntity<ApiResponse<List<QuizQuestionAnalyticsResponse>>> getQuizAnalytics(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", quizService.getQuizAnalytics(courseId, lessonId)));
    }

    @Operation(summary = "Grade essay questions in a PENDING_GRADE quiz session", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/quiz-sessions/{sessionId}/grade-essay")
    public ResponseEntity<ApiResponse<QuizSessionResponse>> gradeEssay(
            @PathVariable String sessionId,
            @Valid @RequestBody GradeEssayRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Essay graded successfully", quizService.gradeEssay(sessionId, request)));
    }
}
