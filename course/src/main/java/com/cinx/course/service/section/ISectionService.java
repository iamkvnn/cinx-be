package com.cinx.course.service.section;

import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.model.Section;

import java.util.List;

public interface ISectionService {
    SectionResponse createSection(String courseId, CreateSectionRequest request);
    SectionResponse updateSection(String courseId, String sectionId, UpdateSectionRequest request);
    List<SectionResponse> reorderSections(String courseId, List<String> sectionIds);
    void deleteSection(String courseId, String sectionId);
    Section editableSection(String courseId, String sectionId);
}
