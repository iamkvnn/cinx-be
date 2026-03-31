package com.cinx.user.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, unique = true)
    private String fcmToken;
    
    private String deviceInfo;
    
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        this.updatedAt = LocalDateTime.now();
    }
}