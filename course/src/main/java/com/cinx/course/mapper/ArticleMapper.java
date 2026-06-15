package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.UpdateArticleLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;
import com.cinx.course.model.ArticleLesson;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ArticleMapper extends
        BaseMapper<ArticleLesson, ArticleLessonResponse>,
        CreateMapper<ArticleLesson, CreateArticleLessonRequest>,
        UpdateMapper<ArticleLesson, UpdateArticleLessonRequest> {
    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "articleUrl", ignore = true)
    @Override
    ArticleLesson toModel(CreateArticleLessonRequest createDto);

    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "articleUrl", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Override
    void partialUpdate(@MappingTarget ArticleLesson model, UpdateArticleLessonRequest updateDto);
}
