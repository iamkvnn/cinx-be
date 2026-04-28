package com.cinx.course.service.videoquestion;

import com.cinx.course.dto.request.CreateVideoQuestionRequest;
import com.cinx.course.dto.request.UpdateVideoQuestionRequest;
import com.cinx.course.dto.response.VideoQuestionResponse;

import java.util.List;

public interface IVideoQuestionService {
    List<VideoQuestionResponse> getQuestionsByLessonId(String lessonId);
    
    VideoQuestionResponse getQuestionById(String id);
    
    VideoQuestionResponse createQuestion(String lessonId, CreateVideoQuestionRequest request);
    
    VideoQuestionResponse updateQuestion(String id, UpdateVideoQuestionRequest request);
    
    void deleteQuestion(String id);
    
    boolean checkAnswer(String questionId, String userAnswer);
}