package com.cinx.learning.messaging.event;

import com.cinx.learning.consts.ScoringMode;
import com.cinx.learning.dto.response.QuizQuestionResponse;
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
