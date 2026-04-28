package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateVideoQuestionRequest;
import com.cinx.course.dto.request.UpdateVideoQuestionRequest;
import com.cinx.course.dto.response.VideoQuestionResponse;
import com.cinx.course.model.VideoQuestion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoQuestionMapper extends BaseMapper<VideoQuestion, VideoQuestionResponse>,
        CreateMapper<VideoQuestion, CreateVideoQuestionRequest>,
        UpdateMapper<VideoQuestion, UpdateVideoQuestionRequest> {
}