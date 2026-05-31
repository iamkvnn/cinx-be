package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateQuizLessonRequest {
    @Min(1)
    @Schema(example = "15")
    private Integer numberOfQuestionPerQuizSession;
    @Min(1)
    @Schema(example = "5")
    private Integer maxAttempt;
    @Min(0)
    @Schema(example = "3600")
    private Integer duration;
    @Schema(example = "true")
    private Boolean isReviewAllowed;
    @Schema(example = "false")
    private Boolean isShowAnswersOnReview;
    @Schema(example = "true")
    private Boolean shuffleQuestions;
    @Schema(example = "true")
    private Boolean shuffleOptions;
    @Schema(example = "HIGHEST")
    private ScoringMode scoringMode;
}
