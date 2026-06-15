package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.UpdateSubtitleTrackRequest;
import com.cinx.course.dto.response.SubtitleTrackResponse;
import com.cinx.course.model.SubtitleTrack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SubtitleTrackMapper extends
        BaseMapper<SubtitleTrack, SubtitleTrackResponse>,
        UpdateMapper<SubtitleTrack, UpdateSubtitleTrackRequest> {

    @Override
    SubtitleTrackResponse toDto(SubtitleTrack model);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "languageCode", ignore = true)
    @Mapping(target = "fileUrl", ignore = true)
    @Mapping(target = "fileKey", ignore = true)
    @Mapping(target = "originalFileKey", ignore = true)
    @Mapping(target = "wordConfidenceFileKey", ignore = true)
    @Mapping(target = "wordConfidenceFileUrl", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "fileType", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "format", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "videoLesson", ignore = true)
    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget SubtitleTrack entity, UpdateSubtitleTrackRequest dto);
}
