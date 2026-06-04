package com.cinx.course.service.lesson;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.MoveLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonPositionResponse;
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
import com.cinx.course.service.section.ISectionService;
import com.cinx.course.utils.OrderIndexUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return lessonRepository.findPublishedByCourse(courseId)
                .stream()
                .map(Lesson::getStableId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getEnrolledLessonIdsByCourseId(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.PUBLISHED && course.getStatus() != CourseStatus.ARCHIVED) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return lessonRepository.findEnrolledReadableByCourse(courseId)
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
    public LessonPositionResponse moveLesson(String courseId, String lessonId, MoveLessonRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        CourseDraft draft = courseDraftService.getOrCreateDraft(course);
        List<Section> draftSections = sectionRepository.findDraftByDraftForUpdate(draft.getId());
        Map<String, Section> sectionsByStableId = draftSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));
        Section targetSection = sectionsByStableId.get(request.targetSectionId());
        if (targetSection == null) {
            throw new BadRequestException("Target section does not belong to this course draft: " + request.targetSectionId());
        }
        Lesson movedLesson = lessonRepository.findDraftLessonForUpdate(draft.getId(), lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        Section sourceSection = movedLesson.getSection();

        List<String> affectedSectionIds = List.of(sourceSection, targetSection).stream()
                .map(Section::getId)
                .distinct()
                .toList();
        List<Lesson> affectedLessons = lessonRepository.findBySectionIdsForUpdate(affectedSectionIds);
        List<Lesson> targetLessons = affectedLessons.stream()
                .filter(lesson -> Objects.equals(lesson.getSection().getStableId(), targetSection.getStableId()))
                .filter(lesson -> !Objects.equals(lesson.getStableId(), lessonId))
                .collect(Collectors.toCollection(ArrayList::new));
        int targetIndex = OrderIndexUtils.insertionIndex(
                targetLessons,
                request.previousLessonId(),
                request.nextLessonId(),
                Lesson::getStableId,
                lessonId,
                "lesson"
        );
        Integer newOrderIndex = OrderIndexUtils.midpointOrderIndex(
                targetLessons,
                targetIndex,
                Lesson::getOrderIndex
        );
        boolean sectionChanged = !Objects.equals(sourceSection.getStableId(), targetSection.getStableId());
        if (newOrderIndex != null) {
            boolean orderChanged = !Objects.equals(movedLesson.getOrderIndex(), newOrderIndex);
            if (sectionChanged || orderChanged) {
                movedLesson.setSection(targetSection);
                movedLesson.setOrderIndex(newOrderIndex);
                lessonRepository.save(movedLesson);
            }
            return new LessonPositionResponse(movedLesson.getStableId(), targetSection.getStableId(), movedLesson.getOrderIndex());
        }

        List<Lesson> desiredTargetLessons = new ArrayList<>(targetLessons);
        desiredTargetLessons.add(targetIndex, movedLesson);
        movedLesson.setSection(targetSection);
        List<Lesson> changedLessons = OrderIndexUtils.rebalance(
                desiredTargetLessons,
                Lesson::getOrderIndex,
                Lesson::setOrderIndex
        );
        if (sectionChanged && !changedLessons.contains(movedLesson)) {
            changedLessons.add(movedLesson);
        }
        if (!changedLessons.isEmpty()) {
            lessonRepository.saveAll(changedLessons);
        }
        return new LessonPositionResponse(movedLesson.getStableId(), targetSection.getStableId(), movedLesson.getOrderIndex());
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
                .orElse(0) + OrderIndexUtils.ORDER_STEP;
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
