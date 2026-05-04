package com.cinx.course.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public record QuizOptionResponse (
        String id,
        String optionText,
        Boolean isCorrect,
        Integer optionOrder,
        String matchText
) {}
