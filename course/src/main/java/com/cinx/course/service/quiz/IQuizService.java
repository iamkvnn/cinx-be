package com.cinx.course.service.quiz;

import com.cinx.course.dto.request.CreateQuizLessonRequest;
import com.cinx.course.dto.response.QuizLessonResponse;

public interface IQuizService {
        QuizLessonResponse getQuizByLessonId(String lessonId);
        void createQuiz(String lessonId, CreateQuizLessonRequest request);
        void updateQuiz(String lessonId, CreateQuizLessonRequest request);
        void deleteQuiz(String lessonId);
}
