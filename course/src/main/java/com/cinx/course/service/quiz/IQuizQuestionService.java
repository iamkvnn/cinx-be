package com.cinx.course.service.quiz;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.dto.request.CreateQuizQuestionRequest;
import com.cinx.course.dto.request.UpdateQuizQuestionRequest;
import com.cinx.course.dto.response.QuizQuestionResponse;
import com.cinx.course.model.QuizOption;

import java.util.List;

public interface IQuizQuestionService {
    List<QuizQuestionResponse> getQuestions(String currentUserId, String courseId, String lessonId);
    QuizQuestionResponse addQuestion(String currentUserId, String courseId, String lessonId, CreateQuizQuestionRequest request);
    List<QuizQuestionResponse> addQuestions(String lessonId, List<CreateQuizQuestionRequest> requests);
    QuizQuestionResponse updateQuestion(String currentUserId, String courseId, String lessonId, String questionId, UpdateQuizQuestionRequest request);
    void deleteQuestion(String currentUserId, String courseId, String lessonId, String questionId);
}
