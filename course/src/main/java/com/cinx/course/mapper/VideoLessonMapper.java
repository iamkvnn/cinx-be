package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateVideoLessonRequest;
import com.cinx.course.dto.request.UpdateVideoLessonRequest;
import com.cinx.course.dto.response.VideoLessonResponse;
import com.cinx.course.model.VideoLesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = SubtitleTrackMapper.class)
public interface VideoLessonMapper extends
        BaseMapper<VideoLesson, VideoLessonResponse>,
        CreateMapper<VideoLesson, CreateVideoLessonRequest>,
        UpdateMapper<VideoLesson, UpdateVideoLessonRequest> {

    @Mapping(target = "hasQuestions", expression = "java(model.getQuestions() != null && !model.getQuestions().isEmpty())")
    @Mapping(target = "questionCount", expression = "java(model.getQuestions() != null ? model.getQuestions().size() : 0)")
    @Mapping(target = "hasSubtitles", expression = "java(model.getSubtitles() != null && !model.getSubtitles().isEmpty())")
    @Mapping(target = "subtitleCount", expression = "java(model.getSubtitles() != null ? model.getSubtitles().size() : 0)")
    @Mapping(target = "subtitles", source = "subtitles")
    @Override
    VideoLessonResponse toDto(VideoLesson model);
}
