package com.cinx.learning.repository;

import com.cinx.learning.model.UserStreak;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, String> {
    Optional<UserStreak> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserStreak s WHERE s.userId = :userId")
    Optional<UserStreak> findForUpdateByUserId(String userId);
}
