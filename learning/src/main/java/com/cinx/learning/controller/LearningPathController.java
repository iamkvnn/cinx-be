package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.LearningPathRequest;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.service.learningPath.ILearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {
    
    private final ILearningPathService learningPathService;

    @PostMapping
    public ResponseEntity<ApiResponse<LearningPathResponse>> createLearningPath(@RequestBody LearningPathRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        LearningPathResponse response = learningPathService.createLearningPath(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning path created successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getActiveLearningPath() {
        String userId = AuthenticationUtil.extractUserId();
        LearningPathResponse response = learningPathService.getActiveLearningPath(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Active learning path retrieved", response));
    }

    @DeleteMapping("/active")
    public ResponseEntity<ApiResponse<Void>> dropActiveLearningPath() {
        String userId = AuthenticationUtil.extractUserId();
        learningPathService.dropActiveLearningPath(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning path dropped successfully", null));
    }
}
