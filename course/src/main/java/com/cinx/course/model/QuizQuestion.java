package com.cinx.course.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    private QuizQuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ScoringMethod scoringMethod = ScoringMethod.ALL_OR_NOTHING;

    @Builder.Default
    private Boolean needSync = false;

    @OneToMany(mappedBy = "quizQuestion")
    private List<QuizOption> options;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_lesson_id")
    private QuizLesson quizLesson;
}
