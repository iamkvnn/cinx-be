package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.learning.consts.QuizSessionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession extends BaseEntity {
    private String quizLessonId;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private QuizSessionStatus status;
    private Boolean isReviewAllowed;
    private Boolean isShowAnswersOnReview;

    @OneToMany(mappedBy = "quizSession")
    private List<QuizSessionQuestion> questions;

    @OneToOne(mappedBy = "quizSession")
    private QuizSessionSubmission quizSessionSubmission;
}
