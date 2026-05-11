package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuizLessonRequest {
    @NotNull
    @Min(1)
    private Integer numberOfQuestionPerQuizSession;
    @Min(1)
    private Integer maxAttempt;
    @Min(0)
    private Integer duration;
    @NotNull
    private Boolean isReviewAllowed;
    @NotNull
    private Boolean isShowAnswersOnReview;
    @NotNull
    private Boolean shuffleQuestions;
    @NotNull
    private Boolean shuffleOptions;
    @NotNull
    private ScoringMode scoringMode;
    @NotEmpty
    @Valid
    private List<CreateQuizQuestionRequest> questions;
}
