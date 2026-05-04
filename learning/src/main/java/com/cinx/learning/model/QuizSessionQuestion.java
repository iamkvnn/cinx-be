package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;
import jakarta.persistence.*;
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
public class QuizSessionQuestion extends BaseEntity {
    private String quizSessionId;
    private String questionId;

    @Enumerated(EnumType.STRING)
    private QuizQuestionType questionType;

    private Integer questionOrder;
    private String correctAnswer;
    private String userAnswer;

    @Enumerated(EnumType.STRING)
    private ScoringMethod scoringMethod;

    private Double score;

    @ManyToOne
    @JoinColumn(name = "quizSessionId", insertable = false, updatable = false)
    private QuizSession quizSession;
}
