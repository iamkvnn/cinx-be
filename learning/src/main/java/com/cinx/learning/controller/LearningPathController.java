package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.LearningPathRequest;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.service.learningPath.ILearningPathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {
    
    private final ILearningPathService learningPathService;

    @Operation(summary = "Get current user's learning paths", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningPathResponse>>> getLearningPaths() {
        String userId = AuthenticationUtil.extractUserId();
        List<LearningPathResponse> response = learningPathService.getLearningPaths(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning paths retrieved successfully", response));
    }

    @Operation(summary = "Get current user's learning path", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getLearningPath(@PathVariable String id) {
        String userId = AuthenticationUtil.extractUserId();
        LearningPathResponse response = learningPathService.getLearningPath(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning path retrieved successfully", response));
    }

    @Operation(summary = "Create current user's learning path", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<LearningPathResponse>> createLearningPath(@Valid @RequestBody LearningPathRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        LearningPathResponse response = learningPathService.createLearningPath(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning path created successfully", response));
    }

    @Operation(summary = "Get current user's active learning path", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getActiveLearningPath() {
        String userId = AuthenticationUtil.extractUserId();
        LearningPathResponse response = learningPathService.getActiveLearningPath(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Active learning path retrieved", response));
    }

    @Operation(summary = "Drop current user's active learning path", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/active")
    public ResponseEntity<ApiResponse<Void>> dropActiveLearningPath() {
        String userId = AuthenticationUtil.extractUserId();
        learningPathService.dropActiveLearningPath(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning path dropped successfully", null));
    }
}
