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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/daily-goals")
@RequiredArgsConstructor
public class DailyGoalController {

    private final IDailyGoalService dailyGoalService;

    @Operation(summary = "Get current user's daily goal for a specific date", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ApiResponse<DailyGoalResponse> getDailyGoal(@RequestParam(required = false) LocalDate date) {
        String userId = AuthenticationUtil.extractUserId();
        LocalDate goalDate = date != null ? date : LocalDate.now();
        UserDailyGoal goal = dailyGoalService.getUserDailyGoal(userId, goalDate);
        
        if (goal == null) {
            return new ApiResponse<>(true, "No daily goal explicitly set for the specified date", null);
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

    @Operation(summary = "Get current user's daily goals for a specific month", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/month")
    public ApiResponse<List<DailyGoalResponse>> getDailyGoalsInMonth(@RequestParam int year, @RequestParam int month) {
        String userId = AuthenticationUtil.extractUserId();
        List<UserDailyGoal> goals = dailyGoalService.getUserDailyGoalsInMonth(userId, year, month);
        
        List<DailyGoalResponse> responses = goals.stream().map(goal -> new DailyGoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTargetXp(),
                goal.getCurrentXp(),
                goal.getGoalDate(),
                goal.getIsCompleted()
        )).collect(Collectors.toList());
        
        return new ApiResponse<>(true, "Monthly daily goals fetched successfully", responses);
    }

    @Operation(summary = "Set current user's daily goal target", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ApiResponse<DailyGoalResponse> setDailyGoal(@Valid @RequestBody SetDailyGoalRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        UserDailyGoal goal = dailyGoalService.setDailyGoal(userId, request.targetXp(), request.goalDate());
        
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
        UserDailyGoal goal = dailyGoalService.setDailyGoal(userId, request.targetXp(), request.goalDate());
        
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

    @Operation(summary = "Delete current user's daily goal for a specific date", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ApiResponse<Void> deleteDailyGoal(@RequestParam(required = false) LocalDate date) {
        String userId = AuthenticationUtil.extractUserId();
        dailyGoalService.deleteDailyGoal(userId, date);
        return new ApiResponse<>(true, "Daily goal deleted successfully", null);
    }
}