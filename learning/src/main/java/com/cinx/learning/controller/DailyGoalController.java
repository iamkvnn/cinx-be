package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.request.SetDailyGoalRequest;
import com.cinx.learning.dto.response.DailyGoalResponse;
import com.cinx.learning.model.UserDailyGoal;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/daily-goals")
@RequiredArgsConstructor
public class DailyGoalController {

    private final IDailyGoalService dailyGoalService;

    @Operation(summary = "Get current user's daily goal", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ApiResponse<DailyGoalResponse> getDailyGoal() {
        String userId = AuthenticationUtil.extractUserId();
        UserDailyGoal goal = dailyGoalService.getUserDailyGoal(userId, LocalDate.now());
        
        if (goal == null) {
            return new ApiResponse<>(true, "No daily goal explicitly set for today", null);
        }

        DailyGoalResponse response = new DailyGoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTargetXp(),
                goal.getCurrentXp(),
                goal.getGoalDate(),
                goal.getIsCompleted()
        );
        
        return new ApiResponse<>(true, "Daily goal fetched successfully", response);
    }

    @Operation(summary = "Set current user's daily goal target", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ApiResponse<DailyGoalResponse> setDailyGoal(@Valid @RequestBody SetDailyGoalRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        UserDailyGoal goal = dailyGoalService.setDailyGoal(userId, request.targetXp());
        
        DailyGoalResponse response = new DailyGoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTargetXp(),
                goal.getCurrentXp(),
                goal.getGoalDate(),
                goal.getIsCompleted()
        );
        
        return new ApiResponse<>(true, "Daily goal updated successfully", response);
    }

    @Operation(summary = "Edit current user's daily goal target", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping
    public ApiResponse<DailyGoalResponse> editDailyGoal(@Valid @RequestBody SetDailyGoalRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        UserDailyGoal goal = dailyGoalService.setDailyGoal(userId, request.targetXp());
        
        DailyGoalResponse response = new DailyGoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTargetXp(),
                goal.getCurrentXp(),
                goal.getGoalDate(),
                goal.getIsCompleted()
        );
        
        return new ApiResponse<>(true, "Daily goal updated successfully", response);
    }

    @Operation(summary = "Delete current user's daily goal", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ApiResponse<Void> deleteDailyGoal() {
        String userId = AuthenticationUtil.extractUserId();
        dailyGoalService.deleteDailyGoal(userId);
        return new ApiResponse<>(true, "Daily goal deleted successfully", null);
    }
}