package com.cinx.learning.service.quiz;

import com.cinx.learning.consts.ScoringMode;
import com.cinx.learning.model.QuizSessionSubmission;
import com.cinx.learning.repository.QuizSessionSubmissionRepository;
import com.cinx.learning.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuizScoreAggregator {
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final CourseService courseService;

    public double aggregateScore(String userId, String quizLessonId) {

        ScoringMode scoringMode = null;
        try {
            scoringMode = courseService.getQuizLessonById(quizLessonId).data().scoringMode();
        } catch (Exception e) {
            log.warn("Could not fetch scoringMode for quizLessonId={}, defaulting to HIGHEST", quizLessonId);
        }
        if (scoringMode == null)
            scoringMode = ScoringMode.HIGHEST;

        return aggregateScore(userId, quizLessonId, scoringMode);
    }

    public double aggregateScore(String userId, String quizLessonId, ScoringMode scoringMode) {
        List<QuizSessionSubmission> allSubmissions = quizSessionSubmissionRepository
                .findAllByUserIdAndQuizLessonId(userId, quizLessonId);

        List<Double> scores = allSubmissions.stream()
                .map(QuizSessionSubmission::getScore)
                .filter(Objects::nonNull)
                .toList();

        return aggregateScore(scores, scoringMode);
    }

    public double aggregateScore(List<Double> scores, ScoringMode scoringMode) {
        return switch (scoringMode) {
            case HIGHEST -> scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case LATEST -> scores.getLast();
            case FIRST -> scores.getFirst();
            case AVERAGE -> scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        };
    }
}
