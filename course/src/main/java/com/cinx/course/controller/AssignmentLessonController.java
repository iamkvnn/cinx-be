package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.dto.request.CreateAssignmentLessonRequest;
import com.cinx.course.dto.request.UpdateAssignmentLessonRequest;
import com.cinx.course.dto.response.AssignmentLessonResponse;
import com.cinx.course.service.assignment.IAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons/{lessonId}/assignments")
@RequiredArgsConstructor
public class AssignmentLessonController {
    private final IAssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<AssignmentLessonResponse>> getAssigmentByLessonId(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", assignmentService.getAssignmentByLessonId(currentUserId, courseId, lessonId))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAssigmentLesson(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody CreateAssignmentLessonRequest request
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        assignmentService.createAssignment(currentUserId, courseId, lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateAssigmentLesson(
            @PathVariable String courseId,
            @PathVariable String lessonId,
            @RequestBody UpdateAssignmentLessonRequest request
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        assignmentService.updateAssignment(currentUserId, courseId, lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
