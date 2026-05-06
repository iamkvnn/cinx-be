package com.cinx.social.service;

import com.cinx.social.dto.request.*;
import com.cinx.social.dto.response.AnswerDto;
import com.cinx.social.dto.response.QuestionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICourseQnAService {
    QuestionDto createQuestion(String userId, CreateQuestionRequest request);
    Page<QuestionDto> getQuestionsByCourse(String courseId, String lessonId, String currentUserId, int page, int size, String sort);
    QuestionDto getQuestionById(String questionId, String currentUserId);
    QuestionDto updateQuestion(String userId, String questionId, UpdateQuestionRequest request);
    void deleteQuestion(String userId, String questionId);
    void upvoteQuestion(String userId, String questionId);
    void reportQuestion(String userId, String questionId, CreateQnAReportRequest request);

    AnswerDto createAnswer(String userId, String questionId, CreateAnswerRequest request);
    Page<AnswerDto> getAnswersForQuestion(String questionId, String currentUserId, int page, int size, String sort);
    Page<AnswerDto> getReplies(String parentAnswerId, String currentUserId, int page, int size, String sort);
    AnswerDto updateAnswer(String userId, String answerId, UpdateAnswerRequest request);
    void deleteAnswer(String userId, String answerId);
    void upvoteAnswer(String userId, String answerId);
    void reportAnswer(String userId, String answerId, CreateQnAReportRequest request);
}
