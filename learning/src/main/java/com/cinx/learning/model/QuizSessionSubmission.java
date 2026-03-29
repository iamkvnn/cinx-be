package com.cinx.learning.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionSubmission extends UserSubmission {
    private String quizSessionId;
    private Integer totalCorrectAnswers;
    private Double score;

    @OneToOne
    @JoinColumn(name = "quizSessionId", insertable = false, updatable = false)
    private QuizSession quizSession;
}
