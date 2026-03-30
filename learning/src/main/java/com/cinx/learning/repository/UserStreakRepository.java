package com.cinx.learning.repository;

import com.cinx.learning.model.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, String> {
    Optional<UserStreak> findByUserId(String userId);
}
