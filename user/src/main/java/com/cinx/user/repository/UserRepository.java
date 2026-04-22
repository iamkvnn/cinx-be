package com.cinx.user.repository;

import com.cinx.user.consts.Role;
import com.cinx.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    @Query("""
        SELECT u FROM User u
        WHERE (:query IS NULL OR u.name LIKE %:query% OR u.email LIKE %:query%)
        AND (:role IS NULL OR u.role = :role)
        AND (:isInstructorVerified IS NULL OR u.isInstructorVerified = :isInstructorVerified)
    """)
    Page<User> findAll(String query, Role role, Boolean isInstructorVerified, Pageable pageable);

    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    boolean existsByEmail(String email);

    List<User> findAllByUserIdIn(List<String> ids);

    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    long countUsersBetween(LocalDateTime start, LocalDateTime end);
}
