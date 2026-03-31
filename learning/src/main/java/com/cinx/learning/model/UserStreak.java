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
public class UserStreak extends BaseEntity {
    private String userId;
    private Integer currentStreak;
    private Integer highestStreak;
    private LocalDate lastActivityDate;
}
