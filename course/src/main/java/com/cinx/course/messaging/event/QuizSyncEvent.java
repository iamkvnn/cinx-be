package com.cinx.course.messaging.event;

import com.cinx.course.consts.ScoringMode;
import com.cinx.course.dto.response.QuizQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSyncEvent {

    private String quizLessonId;
    private String changeReason;
    private ScoringMode scoringMode;
    private List<QuizQuestionResponse> questions;
}
