package com.cinx.learning.service.quiz.evaluator;

import com.cinx.learning.model.QuizSessionQuestion;

import java.util.List;

/**
 * PARTIAL_CREDIT: award a fraction of the mark equal to the proportion of correct elements matched.
 *
 * <ul>
 *   <li>For MULTI_CHOICE: score = (correctly selected correct options) / (total correct options).
 *       No penalty for wrong selections in this mode — use NEGATIVE_MARK for that.</li>
 *   <li>For ORDERING: each token is checked at its positional index; score = matched / total.</li>
 *   <li>For MATCHING: each "optionId:matchText" pair is checked; score = matched / total.</li>
 * </ul>
 */
public class PartialCreditEvaluator implements IQuestionEvaluator {

    @Override
    public double evaluate(QuizSessionQuestion question) {
        List<String> correct = AnswerParser.parseList(question.getCorrectAnswer());
        List<String> user    = AnswerParser.parseList(question.getUserAnswer());

        if (correct.isEmpty()) return 1.0;
        if (user.isEmpty())    return 0.0;

        switch (question.getQuestionType()) {
            case ORDERING -> {
                // Positional match
                int matched = 0;
                int len = Math.min(correct.size(), user.size());
                for (int i = 0; i < len; i++) {
                    if (correct.get(i).equalsIgnoreCase(user.get(i))) matched++;
                }
                return (double) matched / correct.size();
            }
            case MATCHING -> {
                // Each element is "optionId:matchText" — compare as set of pairs
                long matched = user.stream()
                        .filter(correct::contains)
                        .count();
                return (double) matched / correct.size();
            }
            default -> {
                // MULTI_CHOICE (and fallback): set intersection
                long matched = user.stream()
                        .filter(correct::contains)
                        .count();
                return (double) matched / correct.size();
            }
        }
    }
}
