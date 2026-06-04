package com.cinx.course.service.curriculum;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.response.CourseCurriculumResponse;
import com.cinx.course.dto.response.CurriculumSectionResponse;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.course.ICourseDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumService implements ICurriculumService {
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final ICourseDraftService courseDraftService;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional(readOnly = true)
    public CourseCurriculumResponse getPublishedCurriculum(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        List<Section> sections = sectionRepository.findPublishedByCourse(course.getId());
        List<Lesson> lessons = lessonRepository.findPublishedByCourse(course.getId());
        return toResponse(course.getId(), sections, lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCurriculumResponse getDraftCurriculum(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        Optional<CourseDraft> draft = courseDraftService.findDraft(course);
        if (draft.isEmpty()) {
            if (Boolean.TRUE.equals(course.getIsPublished())) {
                return getPublishedCurriculum(courseId);
            }
            return new CourseCurriculumResponse(course.getId(), List.of());
        }
        List<Section> sections = sectionRepository.findDraftByDraft(draft.get().getId());
        List<Lesson> lessons = lessonRepository.findDraftByDraft(draft.get().getId());
        return toResponse(course.getId(), sections, lessons);
    }

    private CourseCurriculumResponse toResponse(String courseId, List<Section> sections, List<Lesson> lessons) {
        Map<String, List<LessonResponse>> lessonsBySectionId = lessons.stream()
                .collect(Collectors.groupingBy(
                        lesson -> lesson.getSection().getStableId(),
                        LinkedHashMap::new,
                        Collectors.mapping(lessonMapper::toResponse, Collectors.toList())
                ));
        List<CurriculumSectionResponse> sectionResponses = sections.stream()
                .map(section -> new CurriculumSectionResponse(
                        section.getStableId(),
                        section.getTitle(),
                        section.getDescription(),
                        section.getDuration(),
                        section.getOrderIndex(),
                        lessonsBySectionId.getOrDefault(section.getStableId(), List.of())
                ))
                .toList();
        return new CourseCurriculumResponse(courseId, sectionResponses);
    }
}
