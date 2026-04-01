package com.cinx.course.service.section;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateSectionRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.model.Course;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.lesson.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService implements ISectionService{
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final ILessonService lessonService;

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
    public List<Section> updateSections(Course course, List<UpdateSectionRequest> request) {
        List<Section> newSections = new ArrayList<>();
        Map<String, Section> existingSectionMap = getSectionsByCourseId(course.getId())
                .stream().collect(Collectors.toMap(Section::getId, section -> section));
        for (UpdateSectionRequest sectionRequest : request) {
            if (sectionRequest.id() != null) {
                Section existingSection = existingSectionMap.get(sectionRequest.id());
                if (existingSection == null) {
                    throw new NotFoundException("Section not found with id: " + sectionRequest.id());
                }
                Map<String, Lesson> existingLessonMap = existingSection.getLessons().stream()
                        .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));
                existingSection.setTitle(sectionRequest.title());
                existingSection.setDescription(sectionRequest.description());
                existingSection.setDuration(sectionRequest.duration());
                existingSection.setOrderIndex(sectionRequest.orderIndex());
                sectionRepository.save(existingSection);
                existingSection.setLessons(sectionRequest.lessons().stream()
                        .map(lessonRequest -> {
                            if (lessonRequest.id() != null) {
                                Lesson existingLesson = existingLessonMap.get(lessonRequest.id());
                                if (existingLesson == null) {
                                    throw new NotFoundException("Lesson not found with id: " + lessonRequest.id());
                                }
                                existingLessonMap.remove(lessonRequest.id());
                                existingLesson.setTitle(lessonRequest.title());
                                existingLesson.setDuration(lessonRequest.duration());
                                existingLesson.setOrderIndex(lessonRequest.orderIndex());
                                return existingLesson;
                            } else {
                                return buildLessonFromUpdateRequest(lessonRequest, existingSection);
                            }
                        }).toList());
                lessonRepository.saveAll(existingSection.getLessons());
                lessonRepository.deleteAll(existingLessonMap.values());
                newSections.add(existingSection);
            } else {
                Section newSection = sectionRepository.save(buildSectionFromUpdateRequest(sectionRequest, course));
                newSection.setLessons(lessonRepository.saveAll(sectionRequest.lessons().stream()
                        .map(lessonRequest -> {
                            if (lessonRequest.id() != null) {
                                throw new BadRequestException("New lesson cannot have an id: " + lessonRequest.id());
                            }
                            return buildLessonFromUpdateRequest(lessonRequest, newSection);
                        }).toList()));
                newSections.add(newSection);
            }
            existingSectionMap.remove(sectionRequest.id());
        }
        sectionRepository.deleteAll(existingSectionMap.values());
        return newSections;
    }

    private Section buildSectionFromUpdateRequest(UpdateSectionRequest sectionRequest, Course course) {
        return Section.builder()
                .title(sectionRequest.title())
                .description(sectionRequest.description())
                .duration(sectionRequest.duration())
                .orderIndex(sectionRequest.orderIndex())
                .course(course)
                .build();
    }

    private Lesson buildLessonFromUpdateRequest(UpdateLessonRequest lessonRequest, Section section) {
        return Lesson.builder()
                .title(lessonRequest.title())
                .duration(lessonRequest.duration())
                .orderIndex(lessonRequest.orderIndex())
                .lessonType(lessonRequest.lessonType())
                .section(section)
                .build();
    }
}
