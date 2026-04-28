package com.cinx.course.dto.request;

import com.cinx.course.consts.QuizQuestionType;
import java.util.List;

public record CreateVideoQuestionRequest(
        String questionText,
        QuizQuestionType questionType,
        Integer timestampSeconds,
        List<CreateVideoOptionRequest> options
) {
}
