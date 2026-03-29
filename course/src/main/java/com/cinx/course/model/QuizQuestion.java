package com.cinx.course.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.course.consts.QuizQuestionType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion extends BaseEntity {
    private String questionText;
    private QuizQuestionType questionType;
    private Integer orderIndex;
    //private Short weight;

    @OneToMany(mappedBy = "quizQuestion")
    private List<QuizOption> options;

    @ManyToOne(fetch = FetchType.LAZY)
    private QuizLesson quizLesson;
}
