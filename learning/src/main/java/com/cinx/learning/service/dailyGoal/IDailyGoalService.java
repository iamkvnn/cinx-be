package com.cinx.learning.service.dailyGoal;

import com.cinx.learning.model.UserDailyGoal;

import java.time.LocalDate;
import java.util.List;

public interface IDailyGoalService {
    UserDailyGoal getUserDailyGoal(String userId, LocalDate date);
    UserDailyGoal setDailyGoal(String userId, Integer targetXp, LocalDate date);
    void addXp(String userId, Integer xpAmount);
    void deleteDailyGoal(String userId, LocalDate date);
    List<UserDailyGoal> getUserDailyGoalsInMonth(String userId, int year, int month);
}