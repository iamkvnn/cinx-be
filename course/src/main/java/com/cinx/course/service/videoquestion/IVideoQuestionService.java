package com.cinx.course.service.videoquestion;

import com.cinx.course.dto.request.CreateVideoQuestionRequest;
import com.cinx.course.dto.request.UpdateVideoQuestionRequest;
import com.cinx.course.dto.response.VideoQuestionResponse;

import java.util.List;

public interface IVideoQuestionService {
    List<VideoQuestionResponse> getQuestionsByLessonId(String currentUserId, String courseId, String lessonId);
    
    VideoQuestionResponse getQuestionById(String currentUserId, String courseId, String lessonId, String id);
    
    VideoQuestionResponse createQuestion(String currentUserId, String courseId, String lessonId, CreateVideoQuestionRequest request);
    
    VideoQuestionResponse updateQuestion(String currentUserId, String courseId, String lessonId, String id, UpdateVideoQuestionRequest request);
    
    void deleteQuestion(String currentUserId, String courseId, String lessonId, String id);
    
    boolean checkAnswer(String questionId, String userAnswer);
}
