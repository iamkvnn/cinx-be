package com.cinx.learning.service.quiz.evaluator;

import com.cinx.learning.model.QuizSessionQuestion;

/**
 * Strategy interface for grading a single {@link QuizSessionQuestion}.
 * Each implementation handles one {@link com.cinx.learning.consts.ScoringMethod}.
 *
 * @return score in the range [0.0, 1.0] representing a fraction of full marks.
 *         The caller multiplies by the question weight (if any) to get the final point value.
 */
public interface IQuestionEvaluator {
    /**
     * Evaluate the user's answer against the correct answer and return a fractional score.
     *
     * @param question the persisted {@link QuizSessionQuestion} (already has userAnswer set)
     * @return score in [0.0, 1.0]
     */
    double evaluate(QuizSessionQuestion question);
}
