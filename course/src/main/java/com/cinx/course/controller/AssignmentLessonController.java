package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/assignment-lessons")
@RequiredArgsConstructor
public class AssignmentLessonController {
    private final IAssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<AssignmentLessonResponse>> getAssigmentByLessonId(@RequestParam String lessonId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", assignmentService.getAssignmentByLessonId(lessonId))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAssigmentLesson(@RequestParam String lessonId, @RequestBody CreateAssignmentLessonRequest request) {
        assignmentService.createAssignment(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateAssigmentLesson(@RequestParam String lessonId, @RequestBody UpdateAssignmentLessonRequest request) {
        assignmentService.updateAssignment(lessonId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> deleteAssigmentLesson(@RequestParam String lessonId) {
        assignmentService.deleteAssignment(lessonId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Success", null)
        );
    }
}
