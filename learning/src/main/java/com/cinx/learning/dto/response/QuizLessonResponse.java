package com.cinx.learning.dto.response;


import com.cinx.learning.consts.ScoringMode;

import java.time.LocalDateTime;
import java.util.List;

public record QuizLessonResponse (
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer numberOfQuestionPerQuizSession,
        Integer maxAttempt,
        Integer duration,
        Boolean isReviewAllowed,
        Boolean isShowAnswersOnReview,
        Boolean shuffleQuestions,
        Boolean shuffleOptions,
        ScoringMode scoringMode,
        List<QuizQuestionResponse> questions
){
}
