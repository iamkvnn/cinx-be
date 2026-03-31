package com.cinx.learning.repository;

import com.cinx.learning.model.UserDailyGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyGoalRepository extends JpaRepository<UserDailyGoal, String> {
    Optional<UserDailyGoal> findByUserIdAndGoalDate(String userId, LocalDate goalDate);
    List<UserDailyGoal> findByUserIdAndGoalDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<UserDailyGoal> findByGoalDateAndIsCompletedFalse(LocalDate goalDate);
}