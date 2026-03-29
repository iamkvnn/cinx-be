package com.cinx.learning.dto.response;


import com.cinx.learning.consts.QuizQuestionType;

import java.util.List;

public record QuizQuestionResponse(
        String id,
        String questionText,
        QuizQuestionType questionType,
        Integer order,
        //Short score,
        List<QuizOptionResponse> options
) {
}
