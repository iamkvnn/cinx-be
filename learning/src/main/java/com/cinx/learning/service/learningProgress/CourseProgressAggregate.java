package com.cinx.learning.service.learningProgress;

record CourseProgressAggregate(
        int totalItems,
        int completedItems,
        double avgScore,
        boolean completed,
        boolean passed
) {
}
