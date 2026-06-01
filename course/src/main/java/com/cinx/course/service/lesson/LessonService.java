package com.cinx.course.service.lesson;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.ReorderLessonsRequest;
import com.cinx.course.dto.request.SectionLessonsOrderRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.dto.response.SectionLessonsOrderResponse;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.course.ICourseDraftService;
import com.cinx.course.service.section.ISectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService implements ILessonService {
    private static final int ORDER_STEP = 1024;

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final ICourseDraftService courseDraftService;
    private final ISectionService sectionService;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional(readOnly = true)
    public List<String> getLessonIdsByCourseId(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!Boolean.TRUE.equals(course.getIsPublished())) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return lessonRepository.findPublishedByCourse(courseId)
                .stream()
                .map(Lesson::getStableId)
                .toList();
    }

    @Override
    @Transactional
    public Lesson getForUpdate(String courseId, String sectionId, String lessonId, LessonType lessonType) {
        Section section = sectionService.editableSection(courseId, sectionId);
        Lesson lesson = lessonRepository.findBySectionAndStableId(section.getId(), lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        lesson.setSection(section);
        if (lessonType != null && lesson.getLessonType() != lessonType) {
            throw new NotFoundException("Lesson not found with id: " + lessonId);
        }
        return lesson;
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureLessonBelongsToCourse(String courseId, String lessonId, LessonType lessonType) {
        List<Lesson> lessons = lessonRepository.findByCourseAndStableId(courseId, lessonId);
        if (lessons.isEmpty()) {
            throw new NotFoundException("Lesson not found with id: " + lessonId);
        }
        if (lessonType != null && lessons.stream().noneMatch(lesson -> lesson.getLessonType() == lessonType)) {
            throw new NotFoundException("Lesson not found with id: " + lessonId);
        }
    }

    @Override
    @Transactional
    public LessonResponse createLesson(String courseId, String sectionId, CreateLessonRequest request) {
        Section section = sectionService.editableSection(courseId, sectionId);
        String stableId = UUID.randomUUID().toString();
        List<String> prerequisiteIds = normalizePrerequisites(section.getDraft().getId(), stableId, request.prerequisiteIds());
        Lesson lesson = lessonMapper.toModel(request);
        lesson.setSection(section);
        lesson.setStableId(stableId);
        lesson.setOrderIndex(nextLessonOrderIndex(section.getId()));
        lesson.setPrerequisiteIds(new ArrayList<>(prerequisiteIds));
        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(String courseId, String sectionId, String lessonId, UpdateLessonRequest request) {
        Lesson lesson = getForUpdate(courseId, sectionId, lessonId, null);
        lessonMapper.partialUpdate(lesson, request);
        if (request.prerequisiteIds() != null) {
            lesson.setPrerequisiteIds(new ArrayList<>(normalizePrerequisites(lesson.getSection().getDraft().getId(), lesson.getStableId(), request.prerequisiteIds())));
        }
        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }

    @Override
    @Transactional
    public List<SectionLessonsOrderResponse> reorderLessons(String courseId, ReorderLessonsRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.getOrCreateDraft(course);
        List<Section> draftSections = sectionRepository.findDraftByDraftForUpdate(draft.getId());
        Map<String, Section> sectionsByStableId = draftSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));

        List<SectionLessonsOrderRequest> requestedSections = validateRequestedSections(request, sectionsByStableId);
        List<String> sectionEntityIds = requestedSections.stream()
                .map(section -> sectionsByStableId.get(section.sectionId()).getId())
                .toList();
        List<Lesson> affectedLessons = sectionEntityIds.isEmpty()
                ? List.of()
                : lessonRepository.findBySectionIdsForUpdate(sectionEntityIds);

        Map<String, List<Lesson>> currentLessonsBySection = lessonsBySection(affectedLessons, requestedSections);
        Map<String, Lesson> lessonsByStableId = affectedLessons.stream()
                .collect(Collectors.toMap(Lesson::getStableId, Function.identity()));
        Map<String, List<Lesson>> desiredLessonsBySection = validateLessonOrder(
                requestedSections,
                currentLessonsBySection,
                lessonsByStableId
        );

        List<Lesson> changedLessons = applySparseLessonOrder(
                currentLessonsBySection,
                desiredLessonsBySection,
                sectionsByStableId,
                lessonsByStableId
        );
        if (!changedLessons.isEmpty()) {
            lessonRepository.saveAll(changedLessons);
        }

        return requestedSections.stream()
                .map(section -> new SectionLessonsOrderResponse(
                        section.sectionId(),
                        desiredLessonsBySection.get(section.sectionId()).stream()
                                .map(lessonMapper::toResponse)
                                .toList()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void deleteLesson(String courseId, String sectionId, String lessonId) {
        Lesson lesson = getForUpdate(courseId, sectionId, lessonId, null);
        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLessonInstructor(String lessonId, String userId) {
        if (userId == null) {
            return false;
        }
        return lessonRepository.isAccessibleByInstructor(lessonId, userId);
    }

    private Integer nextLessonOrderIndex(String sectionId) {
        return lessonRepository.findBySectionIdsForUpdate(List.of(sectionId)).stream()
                .map(Lesson::getOrderIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + ORDER_STEP;
    }

    private List<SectionLessonsOrderRequest> validateRequestedSections(
            ReorderLessonsRequest request,
            Map<String, Section> sectionsByStableId
    ) {
        if (request.sections() == null || request.sections().isEmpty()) {
            throw new BadRequestException("Lesson reorder must include at least one section");
        }

        Set<String> seenSectionIds = new HashSet<>();
        for (SectionLessonsOrderRequest section : request.sections()) {
            if (section.sectionId() == null || !seenSectionIds.add(section.sectionId())) {
                throw new BadRequestException("Lesson reorder contains duplicate or null section id");
            }
            if (!sectionsByStableId.containsKey(section.sectionId())) {
                throw new BadRequestException("Section does not belong to this course draft: " + section.sectionId());
            }
            if (section.lessonIds() == null) {
                throw new BadRequestException("Lesson order must include lesson ids for section: " + section.sectionId());
            }
        }
        return request.sections();
    }

    private Map<String, List<Lesson>> lessonsBySection(
            List<Lesson> lessons,
            List<SectionLessonsOrderRequest> requestedSections
    ) {
        Map<String, List<Lesson>> lessonsBySection = new LinkedHashMap<>();
        requestedSections.forEach(section -> lessonsBySection.put(section.sectionId(), new ArrayList<>()));
        for (Lesson lesson : lessons) {
            String sectionStableId = lesson.getSection().getStableId();
            List<Lesson> sectionLessons = lessonsBySection.get(sectionStableId);
            if (sectionLessons != null) {
                sectionLessons.add(lesson);
            }
        }
        return lessonsBySection;
    }

    private Map<String, List<Lesson>> validateLessonOrder(
            List<SectionLessonsOrderRequest> requestedSections,
            Map<String, List<Lesson>> currentLessonsBySection,
            Map<String, Lesson> lessonsByStableId
    ) {
        Set<String> currentLessonIds = currentLessonsBySection.values().stream()
                .flatMap(List::stream)
                .map(Lesson::getStableId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> requestedLessonIds = new LinkedHashSet<>();
        Map<String, List<Lesson>> desiredLessonsBySection = new LinkedHashMap<>();

        for (SectionLessonsOrderRequest section : requestedSections) {
            List<Lesson> desiredLessons = new ArrayList<>();
            for (String lessonId : section.lessonIds()) {
                if (lessonId == null || !requestedLessonIds.add(lessonId)) {
                    throw new BadRequestException("Lesson order contains duplicate or null lesson id");
                }
                Lesson lesson = lessonsByStableId.get(lessonId);
                if (lesson == null) {
                    throw new BadRequestException("Lesson does not belong to the affected sections: " + lessonId);
                }
                desiredLessons.add(lesson);
            }
            desiredLessonsBySection.put(section.sectionId(), desiredLessons);
        }

        if (!currentLessonIds.equals(requestedLessonIds)) {
            throw new BadRequestException("Lesson order must include exactly the lessons in affected sections");
        }
        return desiredLessonsBySection;
    }

    private List<Lesson> applySparseLessonOrder(
            Map<String, List<Lesson>> currentLessonsBySection,
            Map<String, List<Lesson>> desiredLessonsBySection,
            Map<String, Section> sectionsByStableId,
            Map<String, Lesson> lessonsByStableId
    ) {
        if (sameLessonOrder(currentLessonsBySection, desiredLessonsBySection)) {
            return List.of();
        }

        String movedLessonId = singleMovedLesson(currentLessonsBySection, desiredLessonsBySection);
        if (movedLessonId != null) {
            Lesson movedLesson = lessonsByStableId.get(movedLessonId);
            String targetSectionId = targetSectionId(movedLessonId, desiredLessonsBySection);
            List<Lesson> targetLessons = desiredLessonsBySection.get(targetSectionId);
            Integer newOrderIndex = midpointOrderIndex(targetLessons, targetLessons.indexOf(movedLesson));
            if (newOrderIndex != null) {
                movedLesson.setSection(sectionsByStableId.get(targetSectionId));
                movedLesson.setOrderIndex(newOrderIndex);
                return List.of(movedLesson);
            }
        }

        List<Lesson> changedLessons = new ArrayList<>();
        for (Map.Entry<String, List<Lesson>> entry : desiredLessonsBySection.entrySet()) {
            Section targetSection = sectionsByStableId.get(entry.getKey());
            List<Lesson> desiredLessons = entry.getValue();
            for (int i = 0; i < desiredLessons.size(); i++) {
                Lesson lesson = desiredLessons.get(i);
                int orderIndex = (i + 1) * ORDER_STEP;
                if (!Objects.equals(lesson.getSection().getStableId(), targetSection.getStableId())
                        || !Objects.equals(lesson.getOrderIndex(), orderIndex)) {
                    lesson.setSection(targetSection);
                    lesson.setOrderIndex(orderIndex);
                    changedLessons.add(lesson);
                }
            }
        }
        return changedLessons;
    }

    private boolean sameLessonOrder(
            Map<String, List<Lesson>> currentLessonsBySection,
            Map<String, List<Lesson>> desiredLessonsBySection
    ) {
        for (String sectionId : desiredLessonsBySection.keySet()) {
            if (!lessonIds(currentLessonsBySection.get(sectionId)).equals(lessonIds(desiredLessonsBySection.get(sectionId)))) {
                return false;
            }
        }
        return true;
    }

    private String singleMovedLesson(
            Map<String, List<Lesson>> currentLessonsBySection,
            Map<String, List<Lesson>> desiredLessonsBySection
    ) {
        List<String> candidates = currentLessonsBySection.values().stream()
                .flatMap(List::stream)
                .map(Lesson::getStableId)
                .toList();
        Map<String, String> desiredSectionByLesson = new HashMap<>();
        desiredLessonsBySection.forEach((sectionId, lessons) ->
                lessons.forEach(lesson -> desiredSectionByLesson.put(lesson.getStableId(), sectionId)));

        for (String candidateId : candidates) {
            Map<String, List<String>> moved = lessonIdsBySection(currentLessonsBySection);
            moved.values().forEach(lessonIds -> lessonIds.remove(candidateId));
            String desiredSectionId = desiredSectionByLesson.get(candidateId);
            int desiredIndex = lessonIds(desiredLessonsBySection.get(desiredSectionId)).indexOf(candidateId);
            moved.get(desiredSectionId).add(desiredIndex, candidateId);
            if (moved.equals(lessonIdsBySection(desiredLessonsBySection))) {
                return candidateId;
            }
        }
        return null;
    }

    private Map<String, List<String>> lessonIdsBySection(Map<String, List<Lesson>> lessonsBySection) {
        Map<String, List<String>> idsBySection = new LinkedHashMap<>();
        lessonsBySection.forEach((sectionId, lessons) -> idsBySection.put(sectionId, new ArrayList<>(lessonIds(lessons))));
        return idsBySection;
    }

    private List<String> lessonIds(List<Lesson> lessons) {
        return lessons == null
                ? List.of()
                : lessons.stream().map(Lesson::getStableId).toList();
    }

    private String targetSectionId(String lessonId, Map<String, List<Lesson>> desiredLessonsBySection) {
        return desiredLessonsBySection.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(lesson -> Objects.equals(lesson.getStableId(), lessonId)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Target section not found for lesson: " + lessonId));
    }

    private Integer midpointOrderIndex(List<Lesson> desiredLessons, int movedIndex) {
        Integer previous = movedIndex == 0 ? null : desiredLessons.get(movedIndex - 1).getOrderIndex();
        Integer next = movedIndex == desiredLessons.size() - 1 ? null : desiredLessons.get(movedIndex + 1).getOrderIndex();
        return midpoint(previous, next);
    }

    private Integer midpoint(Integer previous, Integer next) {
        if (previous == null && next == null) {
            return ORDER_STEP;
        }
        if (previous == null) {
            return next > 1 ? next / 2 : null;
        }
        if (next == null) {
            return previous + ORDER_STEP;
        }
        return next - previous > 1 ? previous + (next - previous) / 2 : null;
    }

    private List<String> normalizePrerequisites(String draftId, String targetStableId, List<String> prerequisiteIds) {
        List<String> normalized = prerequisiteIds == null
                ? List.of()
                : prerequisiteIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        if (normalized.contains(targetStableId)) {
            throw new BadRequestException("Lesson cannot be a prerequisite of itself");
        }

        Map<String, Lesson> lessonsByStableId = lessonRepository.findByDraft(draftId).stream()
                .collect(Collectors.toMap(Lesson::getStableId, Function.identity(), (first, ignored) -> first));
        for (String prerequisiteId : normalized) {
            if (!lessonsByStableId.containsKey(prerequisiteId)) {
                throw new BadRequestException("Prerequisite lesson not found: " + prerequisiteId);
            }
        }

        Map<String, List<String>> prerequisiteGraph = new HashMap<>();
        lessonsByStableId.values().forEach(lesson -> prerequisiteGraph.put(
                lesson.getStableId(),
                lesson.getPrerequisiteIds() == null ? List.of() : lesson.getPrerequisiteIds()
        ));
        prerequisiteGraph.put(targetStableId, normalized);

        for (String prerequisiteId : normalized) {
            if (canReach(prerequisiteId, targetStableId, prerequisiteGraph, new HashSet<>())) {
                throw new BadRequestException("Circular lesson prerequisite is not allowed");
            }
        }
        return normalized;
    }

    private boolean canReach(String current, String target, Map<String, List<String>> graph, Set<String> visited) {
        if (Objects.equals(current, target)) {
            return true;
        }
        if (!visited.add(current)) {
            return false;
        }
        return graph.getOrDefault(current, List.of()).stream()
                .anyMatch(next -> canReach(next, target, graph, visited));
    }
}
