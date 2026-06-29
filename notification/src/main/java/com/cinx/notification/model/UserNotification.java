package com.cinx.notification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String userId;
    private String notificationId;
    private Boolean isRead;
    private LocalDateTime sentAt;

    @ManyToOne
    @JoinColumn(name = "notificationId", insertable = false, updatable = false)
    private Notification notification;
}
