package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning/course-progress")
public class LearningProgressController {
    private final ILearningProgressService learningProgressService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCourseProgressByCourseIds(@RequestParam List<String> courseIds) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", learningProgressService.getCourseProgressByCourseIds(userId, courseIds)));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<?>> getCourseProgress(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", learningProgressService.getCourseProgress(userId, courseId)));
    }

    @GetMapping("/{courseId}/items")
    public ResponseEntity<ApiResponse<?>> getLearningItemProgressByCourseId(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", learningProgressService.getLearningItemProgressByCourseId(userId, courseId)));
    }
}
