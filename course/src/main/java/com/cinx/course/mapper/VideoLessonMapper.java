package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateArticleLessonRequest;
import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.response.ArticleLessonResponse;
import com.cinx.course.dto.response.VideoLessonResponse;
import com.cinx.course.model.ArticleLesson;
import com.cinx.course.model.VideoLesson;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoLessonMapper extends
        BaseMapper<VideoLesson, VideoLessonResponse>,
        CreateMapper<VideoLesson, CreateVideoLessonRequest>,
        UpdateMapper<VideoLesson, CreateVideoLessonRequest> {
}
