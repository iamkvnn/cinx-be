package com.cinx.course.model;


import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizOption extends BaseEntity {
    private String questionId;
    private String optionText;
    private Boolean isCorrect;
    private Integer optionOrder;
    private String matchText;

    @ManyToOne
    @JoinColumn(name = "questionId", insertable = false, updatable = false)
    private QuizQuestion quizQuestion;
}
