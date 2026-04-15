package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/course-progress")
public class LearningProgressController {
    private final ILearningProgressService learningProgressService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getCourseProgressByCourseIds(@RequestParam List<String> courseIds) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", learningProgressService.getCourseProgressByCourseIds(userId, courseIds)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseProgressResponse>> getMyCourseProgress(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", learningProgressService.getCourseProgress(userId, courseId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{courseId}/items")
    public ResponseEntity<ApiResponse<List<LearningItemProgressResponse>>> getLearningItemProgressByCourseId(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", learningProgressService.getLearningItemProgressByCourseId(userId, courseId)));
    }

    @Operation(summary = "Mark an item as complete (e.g. Article)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/items/{itemId}/complete")
    public ResponseEntity<ApiResponse<?>> markItemAsComplete(@PathVariable String itemId) {
        String userId = AuthenticationUtil.extractUserId();
        learningProgressService.updateLearningItemProgress(userId, itemId, new UpdateLearningItemRequest(true, true, 10.0));
        return ResponseEntity.ok(new ApiResponse<>(true, "Marked complete", null));
    }

    @Operation(summary = "Get overview progress of students in a course", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getCourseProgress(@PathVariable String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", learningProgressService.getCourseProgressByCourseId(courseId)));
    }

    @Operation(summary = "Get detailed progress of a student in a course", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/courses/{courseId}/students/{studentId}/progress")
    public ResponseEntity<ApiResponse<List<LearningItemProgressResponse>>> getStudentProgress(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", learningProgressService.getLearningItemProgressByCourseId(studentId, courseId)));
    }
}
