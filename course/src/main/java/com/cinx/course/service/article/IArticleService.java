package com.cinx.course.service.article;

import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;

public interface IArticleService {
    ArticleLessonResponse getArticleByLessonId(String lessonId);
    void createArticle(String lessonId, CreateArticleLessonRequest request);
    void updateArticle(String lessonId, UpdateArticleLessonRequest request);
    void deleteArticle(String lessonId);
}
