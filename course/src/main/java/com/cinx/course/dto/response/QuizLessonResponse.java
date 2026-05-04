package com.cinx.course.dto.response;


import com.cinx.course.consts.ScoringMode;

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
