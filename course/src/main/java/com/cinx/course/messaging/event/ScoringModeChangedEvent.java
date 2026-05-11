package com.cinx.course.messaging.event;

import com.cinx.course.consts.ScoringMode;

public record ScoringModeChangedEvent(
        String quizLessonId,
        ScoringMode scoringMode
) {
}
