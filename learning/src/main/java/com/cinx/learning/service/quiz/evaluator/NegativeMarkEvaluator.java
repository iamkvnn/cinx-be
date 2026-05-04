package com.cinx.learning.service.quiz.evaluator;

import com.cinx.learning.model.QuizSessionQuestion;

import java.util.List;

/**
 * NEGATIVE_MARK: award partial credit for correct selections but subtract for wrong selections.
 *
 * <p>Formula: score = (correctly_selected - incorrectly_selected) / total_correct,  clamped to [0, 1].
 * A "wrong selection" is any user token that is NOT in the correct set.
 */
public class NegativeMarkEvaluator implements IQuestionEvaluator {

    @Override
    public double evaluate(QuizSessionQuestion question) {
        List<String> correct = AnswerParser.parseList(question.getCorrectAnswer());
        List<String> user    = AnswerParser.parseList(question.getUserAnswer());

        if (correct.isEmpty()) return 1.0;
        if (user.isEmpty())    return 0.0;

        long correctSelections   = user.stream().filter(correct::contains).count();
        long incorrectSelections = user.stream().filter(t -> !correct.contains(t)).count();

        double raw = ((double) correctSelections - incorrectSelections) / correct.size();
        return Math.max(0.0, Math.min(1.0, raw));
    }
}
