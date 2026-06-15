package com.cinx.course.service.article;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.mapper.ArticleMapper;
import com.cinx.course.model.ArticleLesson;
import com.cinx.course.repository.ArticleLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock
    private ArticleLessonRepository articleLessonRepository;
    @Mock
    private ArticleMapper articleLessonMapper;
    @Mock
    private ILessonService lessonService;
    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleService, "cdnUrl", "https://cdn.example.com");
    }

    @Test
    void createArticleStoresPdfMetadataAndPublicUrl() {
        CreateArticleLessonRequest request = createRequest("courses/articles/article.pdf", "article.pdf", "application/pdf", 1024L);
        when(articleLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());
        when(articleLessonMapper.toModel(request)).thenReturn(ArticleLesson.builder()
                .fileKey(request.getFileKey())
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .fileSize(request.getFileSize())
                .build());

        articleService.createArticle("inst-1", "course-1", "lesson-1", request);

        ArticleLesson saved = capturedSavedArticle();
        assertThat(saved.getLessonId()).isEqualTo("lesson-1");
        assertThat(saved.getArticleUrl()).isEqualTo("https://cdn.example.com/courses/articles/article.pdf");
        assertThat(saved.getFileName()).isEqualTo("article.pdf");
    }

    @Test
    void updateArticleRefreshesPublicUrlWhenFileKeyChanges() {
        ArticleLesson existing = ArticleLesson.builder()
                .lessonId("lesson-1")
                .articleUrl("https://cdn.example.com/courses/articles/old.pdf")
                .fileKey("courses/articles/old.pdf")
                .fileName("old.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();
        UpdateArticleLessonRequest request = updateRequest("courses/articles/new.pdf", "new.pdf", "application/pdf", 2048L);
        when(articleLessonRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(existing));

        articleService.updateArticle("inst-1", "course-1", "lesson-1", request);

        verify(articleLessonMapper).partialUpdate(existing, request);
        assertThat(existing.getArticleUrl()).isEqualTo("https://cdn.example.com/courses/articles/new.pdf");
        verify(articleLessonRepository).save(existing);
    }

    @Test
    void createArticleRejectsNonPdfFile() {
        CreateArticleLessonRequest request = createRequest("courses/articles/article.txt", "article.txt", "text/plain", 1024L);

        assertThatThrownBy(() -> articleService.createArticle("inst-1", "course-1", "lesson-1", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PDF");

        verify(articleLessonRepository, never()).save(any());
    }

    private ArticleLesson capturedSavedArticle() {
        ArgumentCaptor<ArticleLesson> captor = ArgumentCaptor.forClass(ArticleLesson.class);
        verify(articleLessonRepository).save(captor.capture());
        return captor.getValue();
    }

    private CreateArticleLessonRequest createRequest(String fileKey, String fileName, String fileType, Long fileSize) {
        CreateArticleLessonRequest request = new CreateArticleLessonRequest();
        request.setFileKey(fileKey);
        request.setFileName(fileName);
        request.setFileType(fileType);
        request.setFileSize(fileSize);
        return request;
    }

    private UpdateArticleLessonRequest updateRequest(String fileKey, String fileName, String fileType, Long fileSize) {
        UpdateArticleLessonRequest request = new UpdateArticleLessonRequest();
        request.setFileKey(fileKey);
        request.setFileName(fileName);
        request.setFileType(fileType);
        request.setFileSize(fileSize);
        return request;
    }
}
