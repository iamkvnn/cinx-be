package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMode;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateQuizLessonRequest {
    @Min(1)
    private Integer numberOfQuestionPerQuizSession;
    @Min(1)
    private Integer maxAttempt;
    @Min(0)
    private Integer duration;
    private Boolean isReviewAllowed;
    private Boolean isShowAnswersOnReview;
    private Boolean shuffleQuestions;
    private Boolean shuffleOptions;
    private ScoringMode scoringMode;
}
