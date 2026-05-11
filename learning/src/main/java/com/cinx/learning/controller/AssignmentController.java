package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.CreateAssignmentSubmissionRequest;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import com.cinx.learning.service.assignment.IAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/assignment-submissions")
public class AssignmentController {
    private final IAssignmentService assignmentService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/list")
    public ResponseEntity<PaginatedApiResponse<AssignmentSubmissionResponse>> getAssignmentSubmissions(
            @RequestParam String assignmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(PaginationWrapper.wrap(assignmentService.getAssignmentSubmissions(assignmentId, page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> getAssignmentSubmission(@RequestParam String assignmentId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", assignmentService.getAssignmentSubmission(userId, assignmentId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> submitAssignment(
            @RequestParam String assignmentId,
            @RequestBody CreateAssignmentSubmissionRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        assignmentService.submitAssignment(userId, assignmentId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Assignment submitted successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{submissionId}/score")
    public ResponseEntity<ApiResponse<?>> scoreAssignmentSubmission(
            @PathVariable String submissionId,
            @RequestParam Double score
    ) {
        assignmentService.scoreAssignmentSubmission(submissionId, score);
        return ResponseEntity.ok(new ApiResponse<>(true, "Assignment submission scored successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<?>> deleteAssignmentSubmission(@PathVariable String submissionId) {
        assignmentService.deleteAssignmentSubmission(submissionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Assignment submission deleted successfully", null));
    }
}
