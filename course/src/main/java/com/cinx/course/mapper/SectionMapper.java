package com.cinx.course.mapper;

import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.model.Section;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SectionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "stableId", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    Section toModel(CreateSectionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "stableId", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    void partialUpdate(@MappingTarget Section section, UpdateSectionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "draft", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    Section cloneSection(Section section);

    @Mapping(target = "id", source = "stableId")
    SectionResponse toResponse(Section section);
}
