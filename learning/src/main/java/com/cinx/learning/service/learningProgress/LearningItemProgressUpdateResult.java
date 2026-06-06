package com.cinx.learning.service.learningProgress;

public record LearningItemProgressUpdateResult(
        boolean completedTransition,
        boolean passedTransition,
        boolean courseCompletedTransition,
        boolean coursePassedTransition
) {
}
