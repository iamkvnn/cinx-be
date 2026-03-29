package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLessonTrackingHistory extends BaseEntity {
    private String userId;
    private String videoLessonId;
    @Column(nullable = false)
    private Integer currentPosition;
    @Column(nullable = false)
    private LocalDateTime lastTrackingTime;
}
