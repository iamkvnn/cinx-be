package com.cinx.course.dto.response;

import com.cinx.course.consts.QuizQuestionType;
import java.util.List;

public record VideoQuestionResponse(
        String id,
        String questionText,
        QuizQuestionType questionType,
        Integer timestampSeconds,
        List<VideoOptionResponse> options
) {
}