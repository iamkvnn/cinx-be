package com.cinx.course.dto.response;

import com.cinx.course.consts.QuizQuestionType;

import java.util.List;

public record QuizQuestionResponse(
        String id,
        String questionText,
        QuizQuestionType questionType,
        Integer orderIndex,
        //Short weight,
        List<QuizOptionResponse> options
) {
}
