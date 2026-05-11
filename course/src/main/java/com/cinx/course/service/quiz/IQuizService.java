package com.cinx.course.service.quiz;

import com.cinx.course.dto.request.CreateQuizLessonRequest;
import com.cinx.course.dto.request.SyncQuizRequest;
import com.cinx.course.dto.request.UpdateQuizLessonRequest;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.model.QuizLesson;

public interface IQuizService {
    QuizLesson getOrThrow(String lessonId);
    QuizLessonResponse getQuizByLessonId(String lessonId);
    void createQuiz(String lessonId, CreateQuizLessonRequest request);
    void updateQuiz(String lessonId, UpdateQuizLessonRequest request);
    void syncQuiz(String lessonId, SyncQuizRequest request);
}
