package com.cinx.learning.dto.response;


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
        List<QuizQuestionResponse> questions
){
}
