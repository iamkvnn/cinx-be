package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateVideoOptionRequest;
import com.cinx.course.dto.request.UpdateVideoOptionRequest;
import com.cinx.course.dto.response.VideoOptionResponse;
import com.cinx.course.model.VideoOption;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoOptionMapper extends BaseMapper<VideoOption, VideoOptionResponse>,
        CreateMapper<VideoOption, CreateVideoOptionRequest>,
        UpdateMapper<VideoOption, UpdateVideoOptionRequest> {
        
    VideoOption toModel(UpdateVideoOptionRequest request);
}