package com.cinx.course.service.article;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;
import com.cinx.course.mapper.ArticleMapper;
import com.cinx.course.repository.ArticleLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService implements IArticleService {
    private final ArticleLessonRepository articleLessonRepository;
    private final ArticleMapper articleLessonMapper;
    private final ILessonService lessonService;

    @Override
    public ArticleLessonResponse getArticleByLessonId(String currentUserId, String courseId, String lessonId) {
        lessonService.ensureCanReadLessonContent(currentUserId, courseId, lessonId, LessonType.ARTICLE);
        return getArticleOrThrow(lessonId);
    }

    @Override
    public ArticleLessonResponse getReadableArticleByLessonId(String currentUserId, String courseId, String lessonId) {
        lessonService.ensureCanReadLessonContent(currentUserId, courseId, lessonId, LessonType.ARTICLE);
        return getArticleOrThrow(lessonId);
    }

    private ArticleLessonResponse getArticleOrThrow(String lessonId) {
        return articleLessonRepository.findByLessonId(lessonId)
                .map(articleLessonMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Article not found for lessonId: " + lessonId));
    }

    @Override
    public void createArticle(String currentUserId, String courseId, String lessonId, CreateArticleLessonRequest request) {
        lessonService.ensureLessonInstructor(currentUserId, courseId, lessonId, LessonType.ARTICLE);
        articleLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Article already exists for lessonId: " + lessonId);
        },() -> {
            var articleLesson = articleLessonMapper.toModel(request);
            articleLesson.setLessonId(lessonId);
            articleLessonRepository.save(articleLesson);
        });
    }

    @Override
    public void updateArticle(String currentUserId, String courseId, String lessonId, UpdateArticleLessonRequest request) {
        lessonService.ensureLessonInstructor(currentUserId, courseId, lessonId, LessonType.ARTICLE);
        articleLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            articleLessonMapper.partialUpdate(existing, request);
            articleLessonRepository.save(existing);
        },() -> {
            throw new NotFoundException("Article not found for lessonId: " + lessonId);
        });
    }
}
