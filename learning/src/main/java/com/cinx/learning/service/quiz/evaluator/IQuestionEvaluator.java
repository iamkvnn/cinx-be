package com.cinx.learning.service.quiz.evaluator;

import com.cinx.learning.model.QuizSessionQuestion;

public interface IQuestionEvaluator {
    double evaluate(QuizSessionQuestion question);
}
