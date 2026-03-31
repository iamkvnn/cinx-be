package com.cinx.learning.service.dailyGoal;

import com.cinx.learning.model.UserDailyGoal;

import java.time.LocalDate;

public interface IDailyGoalService {
    UserDailyGoal getUserDailyGoal(String userId, LocalDate date);
    UserDailyGoal setDailyGoal(String userId, Integer targetXp);
    void addXp(String userId, Integer xpAmount);
    void deleteDailyGoal(String userId);
}