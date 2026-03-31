package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.dto.response.QuizQuestionAnalyticsResponse;
import com.cinx.learning.service.assessment.IAssignmentService;
import com.cinx.learning.service.instructor.IInstructorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    private final IAssignmentService assignmentService;

    @Operation(summary = "Get overview progress of students in a course", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getCourseProgress(@PathVariable String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", instructorService.getCourseProgressByCourseId(courseId)));
    }

    @Operation(summary = "Get detailed progress of a student in a course", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/courses/{courseId}/students/{studentId}/progress")
    public ResponseEntity<ApiResponse<List<LearningItemProgressResponse>>> getStudentProgress(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", instructorService.getStudentProgressByCourseIdAndStudentId(courseId, studentId)));
    }

    @Operation(summary = "Get analytical statistics for a quiz", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/quizzes/{quizId}/analytics")
    public ResponseEntity<ApiResponse<List<QuizQuestionAnalyticsResponse>>> getQuizAnalytics(@PathVariable String quizId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", instructorService.getQuizAnalytics(quizId)));
    }

    @Operation(summary = "Get submissions for an assignment", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<PaginatedApiResponse<AssignmentSubmissionResponse>> getAssignmentSubmissions(
            @PathVariable String assignmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AssignmentSubmissionResponse> submissions = assignmentService.getAssignmentSubmissions(assignmentId, page, size);
        return ResponseEntity.ok(PaginationWrapper.wrap(submissions));
    }

    @Operation(summary = "Grade an assignment submission", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/assignments/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse<Void>> scoreAssignmentSubmission(
            @PathVariable String submissionId,
            @RequestParam Double score) {
        assignmentService.scoreAssignmentSubmission(submissionId, score);
        return ResponseEntity.ok(new ApiResponse<>(true, "Graded successfully", null));
    }
}