package com.cinx.course.service.section;

import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.model.Course;
import com.cinx.course.model.Section;

import java.util.List;

public interface ISectionService {
    List<Section> getSectionsByCourseId(String courseId);
    List<Section> createSections(List<Section> sections);
    List<Section> updateSections(Course course, List<CreateSectionRequest> request);
}
