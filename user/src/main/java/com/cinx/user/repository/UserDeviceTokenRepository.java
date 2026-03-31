package com.cinx.user.repository;

import com.cinx.user.model.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, String> {
    List<UserDeviceToken> findByUserId(String userId);
    Optional<UserDeviceToken> findByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
}