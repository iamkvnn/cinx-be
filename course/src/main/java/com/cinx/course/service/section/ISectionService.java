package com.cinx.course.service.section;

import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.MoveSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionPositionResponse;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.model.Section;

public interface ISectionService {
    SectionResponse createSection(String courseId, CreateSectionRequest request);
    SectionResponse updateSection(String courseId, String sectionId, UpdateSectionRequest request);
    SectionPositionResponse moveSection(String courseId, String sectionId, MoveSectionRequest request);
    void deleteSection(String courseId, String sectionId);
    Section editableSection(String courseId, String sectionId);
}
