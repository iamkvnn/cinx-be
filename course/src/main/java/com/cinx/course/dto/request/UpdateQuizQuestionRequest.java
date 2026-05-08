package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMethod;

import java.util.List;

public record UpdateQuizQuestionRequest(
        String questionText,
        ScoringMethod scoringMethod,
        List<UpdateQuizOptionRequest> options
) {}
