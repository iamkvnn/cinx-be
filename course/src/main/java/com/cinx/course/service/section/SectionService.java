package com.cinx.course.service.section;

import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.model.Course;
import com.cinx.course.model.Lecture;
import com.cinx.course.model.Section;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.lecture.ILectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService implements ISectionService{
    private final SectionRepository sectionRepository;
    private final ILectureService lectureService;

    @Override
    public List<Section> getSectionsByCourseId(String courseId) {
        List<Section> sections = sectionRepository.findByCourseId(courseId);
        List<String> sectionIds = sections.stream().map(Section::getId).toList();
        Map<String, List<Lecture>> lecturesBySectionId = lectureService.getLecturesBySectionIds(sectionIds)
                .stream().collect(Collectors.groupingBy(lecture -> lecture.getSection().getId()));
        sections.forEach(section -> section.setLectures(lecturesBySectionId.getOrDefault(section.getId(), List.of())));
        return sections;
    }

    @Override
    public List<Section> createSections(List<Section> sections) {
        List<Section> savedSections = sectionRepository.saveAll(sections);
        lectureService.createLectures(
                savedSections.stream()
                        .flatMap(section -> section.getLectures().stream().peek(lecture -> lecture.setSection(section)))
                        .toList()
        );
        return savedSections;
    }

    @Override
    public List<Section> updateSections(Course course, List<CreateSectionRequest> request) {
        return List.of();
    }
}
