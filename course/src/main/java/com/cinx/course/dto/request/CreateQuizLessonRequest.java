package com.cinx.course.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateQuizLessonRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer numberOfQuestionPerQuizSession;
    private Integer maxAttempt;
    private Integer duration;
    private List<CreateQuizQuestionRequest> questions;
}
