package com.cinx.notification.repository;

import com.cinx.notification.model.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {
    Page<UserNotification> findByUserId(String userId, Pageable pageable);
    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.userId = :userId AND un.isRead = false")
    Long countByUserIdAndReadFalse(String userId);
}
