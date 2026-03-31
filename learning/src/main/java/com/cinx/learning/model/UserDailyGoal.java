package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyGoal extends BaseEntity {
    private String userId;
    private Integer targetXp;
    
    @lombok.Builder.Default
    private Integer currentXp = 0;
    
    private LocalDate goalDate;
    
    @lombok.Builder.Default
    private Boolean isCompleted = false;
}