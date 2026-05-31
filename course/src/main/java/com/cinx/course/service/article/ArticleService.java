package com.cinx.course.service.article;

import com.cinx.common.exception.AlreadyExistException;
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
    public ArticleLessonResponse getArticleByLessonId(String lessonId) {
        return articleLessonRepository.findByLessonId(lessonId)
                .map(articleLessonMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Article not found for lessonId: " + lessonId));
    }

    @Override
    public void createArticle(String lessonId, CreateArticleLessonRequest request) {
        articleLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            throw new AlreadyExistException("Article already exists for lessonId: " + lessonId);
        },() -> {
            var articleLesson = articleLessonMapper.toModel(request);
            articleLesson.setLessonId(lessonId);
            articleLessonRepository.save(articleLesson);
        });
    }

    @Override
    public void updateArticle(String lessonId, UpdateArticleLessonRequest request) {
        articleLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            articleLessonMapper.partialUpdate(existing, request);
            articleLessonRepository.save(existing);
        },() -> {
            throw new NotFoundException("Article not found for lessonId: " + lessonId);
        });
    }
}
