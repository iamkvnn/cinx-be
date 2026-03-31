package com.cinx.learning.service.dailyGoal;

import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.model.UserDailyGoal;
import com.cinx.learning.repository.UserDailyGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import com.cinx.learning.service.user.UserService;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyGoalService implements IDailyGoalService {

    private final UserDailyGoalRepository dailyGoalRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDailyGoal getUserDailyGoal(String userId, LocalDate date) {
        return dailyGoalRepository.findByUserIdAndGoalDate(userId, date)
                .orElse(null); // Return null instead of default value if no goal exists for the day
    }

    @Override
    public UserDailyGoal setDailyGoal(String userId, Integer targetXp) {
        LocalDate today = LocalDate.now();
        UserDailyGoal goal = dailyGoalRepository.findByUserIdAndGoalDate(userId, today)
                .orElse(UserDailyGoal.builder()
                        .userId(userId)
                        .goalDate(today)
                        .currentXp(0)
                        .isCompleted(false)
                        .build());
        
        goal.setTargetXp(targetXp);
        
        // Check if updating target makes it completed
        if (goal.getCurrentXp() >= goal.getTargetXp()) {
            goal.setIsCompleted(true);
        } else {
            goal.setIsCompleted(false);
        }

        return dailyGoalRepository.save(goal);
    }

    @Override
    public void addXp(String userId, Integer xpAmount) {
        LocalDate today = LocalDate.now();
        dailyGoalRepository.findByUserIdAndGoalDate(userId, today)
                .ifPresent(goal -> {
                    goal.setCurrentXp(goal.getCurrentXp() + xpAmount);
                    if (goal.getCurrentXp() >= goal.getTargetXp() && !goal.getIsCompleted()) {
                        goal.setIsCompleted(true);
                    }
                    dailyGoalRepository.save(goal);
                });
        try {
            userService.addXp(userId, xpAmount);
        } catch (Exception e) {
            System.err.println("Failed to add XP to user profile: " + e.getMessage());
            // log exception if feign fails, but don't fail the transaction
        }
    }

    @Override
    public void deleteDailyGoal(String userId) {
        LocalDate today = LocalDate.now();
        dailyGoalRepository.findByUserIdAndGoalDate(userId, today)
                .ifPresent(dailyGoalRepository::delete);
    }
}