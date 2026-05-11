package com.cinx.learning.service.quiz;

import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.GradeEssayRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.response.QuizQuestionAnalyticsResponse;
import com.cinx.learning.dto.response.QuizSessionQuestionResponse;
import com.cinx.learning.dto.response.QuizSessionResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IQuizService {
    Page<QuizSessionResponse> getQuizSessions(String userId, String quizLessonId, int page, int size);
    QuizSessionResponse getQuizSession(String id);
    Page<QuizSessionQuestionResponse> getQuizSessionQuestions(String quizSessionId, int page, int size);
    QuizSessionResponse createQuizSession(String userId, String quizLessonId);
    void chooseQuizSessionQuestion(String quizSessionId, ChooseQuizAnswerRequest request);
    QuizSessionResponse submitQuizSession(String quizSessionId, SubmitQuizSessionRequest request);
    QuizSessionResponse gradeEssay(String sessionId, GradeEssayRequest request);

    List<QuizQuestionAnalyticsResponse> getQuizAnalytics(String quizId);
}
