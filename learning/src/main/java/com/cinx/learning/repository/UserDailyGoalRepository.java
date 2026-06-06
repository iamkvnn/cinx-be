package com.cinx.learning.repository;

import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.model.UserDailyGoal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyGoalRepository extends JpaRepository<UserDailyGoal, String> {
    List<UserDailyGoal> findByUserIdAndGoalDate(String userId, LocalDate goalDate);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserDailyGoal> findByUserIdAndGoalDateAndGoalKey(String userId, LocalDate goalDate, String goalKey);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserDailyGoal> findByUserIdAndGoalDateAndGoalTypeAndTargetItemId(
            String userId,
            LocalDate goalDate,
            DailyGoalType goalType,
            String targetItemId);
    List<UserDailyGoal> findByUserIdAndGoalDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<UserDailyGoal> findByGoalDateAndIsCompletedFalse(LocalDate goalDate);
}
