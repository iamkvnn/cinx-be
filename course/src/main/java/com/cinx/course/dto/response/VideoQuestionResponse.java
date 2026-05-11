package com.cinx.course.dto.response;

import com.cinx.course.consts.VideoQuizQuestionType;

import java.util.List;

public record VideoQuestionResponse(
        String id,
        String questionText,
        VideoQuizQuestionType questionType,
        Integer timestampSeconds,
        List<VideoOptionResponse> options
) {
}