package com.cinx.learning.mapper;

import com.cinx.learning.dto.request.CreateVideoNoteRequest;
import com.cinx.learning.dto.request.UpdateVideoNoteRequest;
import com.cinx.learning.dto.response.VideoNoteDto;
import com.cinx.learning.model.VideoNote;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VideoNoteMapper {
    VideoNoteDto toDto(VideoNote entity);
    VideoNote toModel(CreateVideoNoteRequest request);
    void partialUpdate(@MappingTarget VideoNote entity, UpdateVideoNoteRequest dto);
}
