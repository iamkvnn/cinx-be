package com.cinx.course.service.article;

import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;

public interface IArticleService {
    ArticleLessonResponse getArticleByLessonId(String currentUserId, String courseId, String lessonId);
    ArticleLessonResponse getReadableArticleByLessonId(String currentUserId, String courseId, String lessonId);
    void createArticle(String currentUserId, String courseId, String lessonId, CreateArticleLessonRequest request);
    void updateArticle(String currentUserId, String courseId, String lessonId, UpdateArticleLessonRequest request);
}
