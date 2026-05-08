package com.cinx.course.messaging.event;

import com.cinx.course.consts.ScoringMethod;
import com.cinx.course.consts.ScoringMode;
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
