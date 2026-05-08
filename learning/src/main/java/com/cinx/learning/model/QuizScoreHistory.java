package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_score_history")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizScoreHistory extends BaseEntity {
    private String quizSessionId;
    private Double oldScore;
    private Double newScore;
    private String reason;
    private LocalDateTime gradedAt;
}
