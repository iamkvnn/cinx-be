package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.learning.dto.request.GradeEssayRequest;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.dto.response.QuizQuestionAnalyticsResponse;
import com.cinx.learning.dto.response.QuizSessionResponse;
import com.cinx.learning.service.assessment.IAssignmentService;
import com.cinx.learning.service.instructor.IInstructorService;
import com.cinx.learning.service.quiz.IQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/instructor")
public class InstructorLearningController {
    private final IInstructorService instructorService;
    private final IQuizService quizService;

    @Operation(summary = "Get analytical statistics for a quiz", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/quizzes/{quizId}/analytics")
    public ResponseEntity<ApiResponse<List<QuizQuestionAnalyticsResponse>>> getQuizAnalytics(@PathVariable String quizId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", instructorService.getQuizAnalytics(quizId)));
    }

    @Operation(summary = "List all PENDING_GRADE sessions for a quiz lesson", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/quiz-lessons/{quizLessonId}/pending-grade")
    public ResponseEntity<PaginatedApiResponse<QuizSessionResponse>> getPendingGradeSessions(
            @PathVariable String quizLessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(quizService.getPendingGradeSessions(quizLessonId, page, size)));
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