package com.cinx.course.service.section;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.change.ICourseChangeAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService implements ISectionService{
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final ICourseChangeAuditService courseChangeAuditService;
    private final SectionMapper sectionMapper;

    @Override
    public List<Section> getSectionsByCourseId(String courseId) {
        List<Section> sections = sectionRepository.findByCourseId(courseId);
        List<String> sectionIds = sections.stream().map(Section::getId).toList();
        Map<String, List<Lesson>> lessonServicesBySectionId = lessonRepository.findAllBySectionIdIn(sectionIds)
                .stream().collect(Collectors.groupingBy(lesson -> lesson.getSection().getId()));
        sections.forEach(section -> section.setLessons(lessonServicesBySectionId.getOrDefault(section.getId(), List.of())));
        return sections;
    }

    @Override
    public Section getSectionById(String courseId, String sectionId) {
        Section section = sectionRepository.findByIdAndCourseId(sectionId, courseId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));
        section.setLessons(lessonRepository.findAllBySectionId(sectionId));
        return section;
    }

    @Transactional
    @Override
    public Section createSection(String courseId, CreateSectionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        course.setStatus(CourseStatus.DRAFT);
        courseRepository.save(course);

        Section section = Section.builder()
                .title(request.title())
                .description(request.description())
                .duration(request.duration())
                .orderIndex(request.orderIndex())
                .course(course)
                .build();
        Section saved = sectionRepository.save(section);
        courseChangeAuditService.auditCourseItemChange(courseId, saved.getId(), null, sectionMapper.toDto(saved));
        return saved;
    }

    @Transactional
    @Override
    public Section updateSection(String courseId, String sectionId, UpdateSectionRequest request) {
        Section section = sectionRepository.findByIdAndCourseId(sectionId, courseId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));
        SectionResponse oldValue = sectionMapper.toDto(section);
        section.getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(section.getCourse());
        sectionMapper.partialUpdate(section, request);
        courseChangeAuditService.auditCourseItemChange(courseId, sectionId, oldValue, sectionMapper.toDto(section));
        return sectionRepository.save(section);
    }

    @Transactional
    @Override
    public void deleteSection(String courseId, String sectionId) {
        Section section = sectionRepository.findByIdAndCourseId(sectionId, courseId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));
        section.getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(section.getCourse());
        courseChangeAuditService.auditCourseItemChange(courseId, sectionId, sectionMapper.toDto(section), null);
        sectionRepository.delete(section);
    }
}
