package com.cinx.course.service.article;

import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;

public interface IArticleService {
    ArticleLessonResponse getArticleByLessonId(String lessonId);
    void createArticle(String lessonId, CreateArticleLessonRequest request);
    void updateArticle(String lessonId, CreateArticleLessonRequest request);
    void deleteArticle(String lessonId);
}
