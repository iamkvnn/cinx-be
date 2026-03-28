package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.learning.consts.QuizQuestionType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private QuizQuestionType questionType;
    private Integer questionOrder;
    private String correctAnswer;
    private String userAnswer;
    private Short score;

    @ManyToOne
    @JoinColumn(name = "quizSessionId", insertable = false, updatable = false)
    private QuizSession quizSession;
}
