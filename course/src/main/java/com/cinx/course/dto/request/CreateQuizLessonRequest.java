package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuizLessonRequest {
    @NotNull
    private Integer numberOfQuestionPerQuizSession;
    private Integer maxAttempt;
    private Integer duration;
    private Boolean isReviewAllowed;
    private Boolean isShowAnswersOnReview;
    private Boolean shuffleQuestions;
    private Boolean shuffleOptions;
    private ScoringMode scoringMode;
    @NotEmpty
    private List<CreateQuizQuestionRequest> questions;
}
