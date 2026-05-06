package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.social.dto.request.CreateAnswerRequest;
import com.cinx.social.dto.request.CreateQuestionRequest;
import com.cinx.social.dto.request.UpdateAnswerRequest;
import com.cinx.social.dto.request.UpdateQuestionRequest;
import com.cinx.social.dto.response.AnswerDto;
import com.cinx.social.dto.response.QuestionDto;
import com.cinx.social.service.ICourseQnAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course-qna")
@RequiredArgsConstructor
public class CourseQnAController {
    private final ICourseQnAService qnaService;

    @PostMapping("/questions")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<QuestionDto>> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Question created successfully", qnaService.createQuestion(userId, request)));
    }

    @GetMapping("/questions")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PaginatedApiResponse<QuestionDto>> getQuestions(
            @RequestParam String courseId,
            @RequestParam(required = false) String lessonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        String userId = AuthenticationUtil.extractUserId();
        Page<QuestionDto> questionDtoPage = qnaService.getQuestionsByCourse(courseId, lessonId, userId, page, size, sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(questionDtoPage));
    }

    @GetMapping("/questions/{questionId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<QuestionDto>> getQuestionById(@PathVariable String questionId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Question details fetched successfully", qnaService.getQuestionById(questionId, userId)));
    }

    @GetMapping("/questions/{questionId}/answers")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PaginatedApiResponse<AnswerDto>> getAnswersForQuestion(
            @PathVariable String questionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        String userId = AuthenticationUtil.extractUserId();
        Page<AnswerDto> answerDtoPage = qnaService.getAnswersForQuestion(questionId, userId, page, size, sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(answerDtoPage));
    }

    @PutMapping("/questions/{questionId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
            @PathVariable String questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Question updated successfully", qnaService.updateQuestion(userId, questionId, request)));
    }

    @DeleteMapping("/questions/{questionId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable String questionId) {
        String userId = AuthenticationUtil.extractUserId();
        qnaService.deleteQuestion(userId, questionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question deleted successfully", null));
    }

    @PostMapping("/questions/{questionId}/upvote")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> upvoteQuestion(@PathVariable String questionId) {
        String userId = AuthenticationUtil.extractUserId();
        qnaService.upvoteQuestion(userId, questionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question upvoted successfully", null));
    }

    @PostMapping("/questions/{questionId}/report")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> reportQuestion(
            @PathVariable String questionId,
            @Valid @RequestBody com.cinx.social.dto.request.CreateQnAReportRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        qnaService.reportQuestion(userId, questionId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question reported successfully", null));
    }

    @PostMapping("/answers")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<AnswerDto>> createAnswer(@Valid @RequestBody CreateAnswerRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer created successfully", qnaService.createAnswer(userId, request.getQuestionId(), request)));
    }

    @PutMapping("/answers/{answerId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<AnswerDto>> updateAnswer(
            @PathVariable String answerId,
            @Valid @RequestBody UpdateAnswerRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer updated successfully", qnaService.updateAnswer(userId, answerId, request)));
    }

    @DeleteMapping("/answers/{answerId}")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(@PathVariable String answerId) {
        String userId = AuthenticationUtil.extractUserId();
        qnaService.deleteAnswer(userId, answerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer deleted successfully", null));
    }

    @PostMapping("/answers/{answerId}/upvote")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> upvoteAnswer(@PathVariable String answerId) {
        String userId = AuthenticationUtil.extractUserId();
        qnaService.upvoteAnswer(userId, answerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer upvoted successfully", null));
    }

    @PostMapping("/answers/{answerId}/report")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> reportAnswer(
            @PathVariable String answerId,
            @Valid @RequestBody com.cinx.social.dto.request.CreateQnAReportRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        qnaService.reportAnswer(userId, answerId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Answer reported successfully", null));
    }

    @GetMapping("/answers/{answerId}/replies")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PaginatedApiResponse<AnswerDto>> getReplies(
            @PathVariable String answerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        String userId = AuthenticationUtil.extractUserId();
        Page<AnswerDto> answerDtoPage = qnaService.getReplies(answerId, userId, page, size, sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(answerDtoPage));
    }
}
