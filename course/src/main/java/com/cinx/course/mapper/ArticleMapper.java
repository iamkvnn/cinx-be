package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;
import com.cinx.course.model.ArticleLesson;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleMapper extends
        BaseMapper<ArticleLesson, ArticleLessonResponse>,
        CreateMapper<ArticleLesson, CreateArticleLessonRequest>,
        UpdateMapper<ArticleLesson, UpdateArticleLessonRequest> {
}
