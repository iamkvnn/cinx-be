package com.cinx.learning.service.quiz.evaluator;

import com.cinx.learning.model.QuizSessionQuestion;

import java.util.List;

/**
 * ALL_OR_NOTHING: the user must supply exactly the same set (unordered) of tokens as the correct answer.
 * Works for SINGLE_CHOICE and MULTI_CHOICE.
 * Returns 1.0 if match, 0.0 otherwise.
 */
public class AllOrNothingEvaluator implements IQuestionEvaluator {

    @Override
    public double evaluate(QuizSessionQuestion question) {
        List<String> correct = AnswerParser.parseList(question.getCorrectAnswer());
        List<String> user    = AnswerParser.parseList(question.getUserAnswer());

        if (correct.isEmpty() && user.isEmpty()) return 1.0;
        if (correct.size() != user.size())        return 0.0;

        // Order-insensitive comparison
        List<String> sortedCorrect = correct.stream().sorted().toList();
        List<String> sortedUser    = user.stream().sorted().toList();
        return sortedCorrect.equals(sortedUser) ? 1.0 : 0.0;
    }
}
