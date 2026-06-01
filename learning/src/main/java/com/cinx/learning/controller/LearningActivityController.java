package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.LearningActivityRequest;
import com.cinx.learning.service.activity.ILearningActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning/activity")
@RequiredArgsConstructor
public class LearningActivityController {
    private final ILearningActivityService learningActivityService;

    @Operation(summary = "Record learning activity heartbeat", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> recordActivity(@Valid @RequestBody LearningActivityRequest request) {
        learningActivityService.recordActivity(AuthenticationUtil.extractUserId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Learning activity recorded successfully", null));
    }
}
