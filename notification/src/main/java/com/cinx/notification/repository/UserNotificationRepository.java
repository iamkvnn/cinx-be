package com.cinx.notification.repository;

import com.cinx.notification.model.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {
    @Query("SELECT un FROM UserNotification un JOIN un.notification n WHERE un.userId = :userId AND " +
            "(:query IS NULL OR n.title LIKE %:query% OR n.message LIKE %:query%)")
    Page<UserNotification> findByUserId(String query, String userId, Pageable pageable);
    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.userId = :userId AND un.isRead = false")
    Long countByUserIdAndReadFalse(String userId);
}
