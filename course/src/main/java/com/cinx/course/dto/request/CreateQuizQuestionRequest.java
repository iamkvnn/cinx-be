package com.cinx.course.dto.request;

import com.cinx.course.consts.QuizQuestionType;

import java.util.List;

public record CreateQuizQuestionRequest(
        String questionText,
        QuizQuestionType questionType,
        Integer orderIndex,
        //Short weight,
        List<CreateQuizOptionRequest> options
) {
}
