package com.cinx.course.model;

import com.cinx.course.consts.ScoringMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizLesson {
    @Id
    private String lessonId;
    private Integer duration;
    private Integer numberOfQuestionPerQuizSession;
    private Integer maxAttempt;

    @Builder.Default
    private Boolean isReviewAllowed = false;

    @Builder.Default
    private Boolean isShowAnswersOnReview = false;

    @Builder.Default
    private Boolean shuffleQuestions = false;

    @Builder.Default
    private Boolean shuffleOptions = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ScoringMode scoringMode = ScoringMode.HIGHEST;

    @Builder.Default
    private Boolean hasPendingSync = false;

    @OneToMany(mappedBy = "quizLesson")
    private List<QuizQuestion> questions;
}
