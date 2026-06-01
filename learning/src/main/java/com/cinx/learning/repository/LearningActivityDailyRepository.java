package com.cinx.learning.repository;

import com.cinx.learning.model.LearningActivityDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LearningActivityDailyRepository extends JpaRepository<LearningActivityDaily, String> {
    Optional<LearningActivityDaily> findByUserIdAndCourseIdAndActivityDate(String userId, String courseId, LocalDate activityDate);

    @Query("SELECT COALESCE(SUM(a.activeSeconds), 0) FROM LearningActivityDaily a WHERE a.userId = :userId")
    Long sumActiveSecondsByUserId(String userId);

    @Query("SELECT FUNCTION('DATE_FORMAT', a.activityDate, '%Y-%m'), COALESCE(SUM(a.activeSeconds), 0) " +
           "FROM LearningActivityDaily a " +
           "WHERE a.userId = :userId AND a.activityDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', a.activityDate, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', a.activityDate, '%Y-%m') ASC")
    List<Object[]> aggregateUserActivityByMonth(String userId, LocalDate startDate, LocalDate endDate);
}
