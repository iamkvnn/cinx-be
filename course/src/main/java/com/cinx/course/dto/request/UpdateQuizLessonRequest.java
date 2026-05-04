package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateQuizLessonRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numberOfQuestionPerQuizSession;
    private Integer maxAttempt;
    private Integer duration;
    private Boolean isReviewAllowed;
    private Boolean isShowAnswersOnReview;
    private Boolean shuffleQuestions;
    private Boolean shuffleOptions;
    private ScoringMode scoringMode;
    private List<CreateQuizQuestionRequest> questions;
}
