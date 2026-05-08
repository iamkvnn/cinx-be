package com.cinx.learning.messaging.event;

import com.cinx.learning.consts.ScoringMethod;
import com.cinx.learning.consts.ScoringMode;
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
    private List<QuestionSnapshot> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionSnapshot {
        private String questionId;
        private String questionText;
        private ScoringMethod scoringMethod;
        private String correctAnswer;
    }
}
