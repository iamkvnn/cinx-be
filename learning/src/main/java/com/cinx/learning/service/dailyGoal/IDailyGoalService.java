package com.cinx.learning.service.dailyGoal;

import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.dto.request.SetDailyGoalRequest;
import com.cinx.learning.model.UserDailyGoal;

import java.time.LocalDate;
import java.util.List;

public interface IDailyGoalService {
    List<UserDailyGoal> getUserDailyGoals(String userId, LocalDate date);
    UserDailyGoal setDailyGoal(String userId, SetDailyGoalRequest request);
    void recordProgress(String userId, DailyGoalType goalType, Integer amount);
    void recordLessonCompleted(String userId, String itemId);
    void deleteDailyGoal(String userId, LocalDate date, DailyGoalType goalType, String targetItemId);
    List<UserDailyGoal> getUserDailyGoalsInMonth(String userId, int year, int month);
}
