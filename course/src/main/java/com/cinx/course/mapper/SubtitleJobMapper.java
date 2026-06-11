package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.course.dto.response.SubtitleJobResponse;
import com.cinx.course.model.SubtitleJob;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubtitleJobMapper extends BaseMapper<SubtitleJob, SubtitleJobResponse> {
    @Override
    SubtitleJobResponse toDto(SubtitleJob model);
}
