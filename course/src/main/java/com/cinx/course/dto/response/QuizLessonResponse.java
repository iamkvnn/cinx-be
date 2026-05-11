package com.cinx.course.dto.response;


import com.cinx.course.consts.ScoringMode;

import java.time.LocalDateTime;
import java.util.List;

public record QuizLessonResponse(
        String lessonId,
        Integer numberOfQuestionPerQuizSession,
        Integer maxAttempt,
        Integer duration,
        Boolean isReviewAllowed,
        Boolean isShowAnswersOnReview,
        Boolean shuffleQuestions,
        Boolean shuffleOptions,
        ScoringMode scoringMode,
        Boolean hasPendingSync,
        List<QuizQuestionResponse> questions
){
}
