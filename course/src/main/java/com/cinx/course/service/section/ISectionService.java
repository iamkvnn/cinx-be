package com.cinx.course.service.section;

import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.model.Section;

import java.util.List;

public interface ISectionService {
    List<Section> getSectionsByCourseId(String courseId);

    Section getSectionById(String courseId, String sectionId);

    Section createSection(String courseId, CreateSectionRequest request);

    Section updateSection(String courseId, String sectionId, UpdateSectionRequest request);

    void deleteSection(String courseId, String sectionId);
}
