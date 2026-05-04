package com.cinx.learning.service.quiz.evaluator;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;
import com.cinx.learning.model.QuizSessionQuestion;

/**
 * Factory that resolves the correct {@link IQuestionEvaluator} for a given question.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>ESSAY questions always use {@link EssayEvaluator}, regardless of scoringMethod.</li>
 *   <li>SHORT_TEXT questions use {@link AllOrNothingEvaluator} (exact string match).</li>
 *   <li>All others delegate to the evaluator matching the question's {@link ScoringMethod}.</li>
 * </ol>
 */
public final class QuestionEvaluatorFactory {

    private static final IQuestionEvaluator ALL_OR_NOTHING = new AllOrNothingEvaluator();
    private static final IQuestionEvaluator PARTIAL_CREDIT = new PartialCreditEvaluator();
    private static final IQuestionEvaluator NEGATIVE_MARK  = new NegativeMarkEvaluator();
    private static final IQuestionEvaluator ESSAY          = new EssayEvaluator();

    private QuestionEvaluatorFactory() {}

    public static IQuestionEvaluator resolve(QuizSessionQuestion question) {
        if (question.getQuestionType() == QuizQuestionType.ESSAY) {
            return ESSAY;
        }
        if (question.getQuestionType() == QuizQuestionType.SHORT_TEXT) {
            return ALL_OR_NOTHING;
        }
        if (question.getScoringMethod() == null) {
            return ALL_OR_NOTHING;
        }
        return switch (question.getScoringMethod()) {
            case PARTIAL_CREDIT -> PARTIAL_CREDIT;
            case NEGATIVE_MARK  -> NEGATIVE_MARK;
            default             -> ALL_OR_NOTHING;
        };
    }
}
