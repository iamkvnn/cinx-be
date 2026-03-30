package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.response.UserStreakResponse;
import com.cinx.learning.service.streak.IStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final IStreakService streakService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserStreakResponse>> getMyStreak() {
        String userId = AuthenticationUtil.extractUserId();
        UserStreakResponse response = streakService.getUserStreak(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Streak retrieved successfully", response));
    }
}
