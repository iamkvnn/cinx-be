package com.cinx.learning.messaging.event;

import com.cinx.learning.consts.ScoringMode;

public record ScoringModeChangedEvent(
        String quizLessonId,
        ScoringMode scoringMode
) {
}
