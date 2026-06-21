package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.messaging.event.LessonChangedEvent;
import com.cinx.course.model.Category;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseDraftRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseDraftService implements ICourseDraftService {
    private final CourseDraftRepository courseDraftRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final CourseMapper courseMapper;
    private final SectionMapper sectionMapper;
    private final LessonMapper lessonMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseDraft> findDraft(Course course) {
        return courseDraftRepository.findByCourse(course.getId());
    }

    @Override
    @Transactional
    public CourseDraft getOrCreateDraft(Course course) {
        return findDraft(course).orElseGet(() -> {
            course.setPublishStatus(null);
            courseRepository.save(course);
            return clonePublishedToDraft(course);
        });
    }

    @Override
    @Transactional
    public CourseDraft updateDraft(Course course, UpdateCourseRequest request, Category category, Long discountRate) {
        CourseDraft draft = getOrCreateDraft(course);
        courseMapper.partialUpdate(draft, request);
        draft.setCategory(category);
        draft.setDiscountRate(discountRate);
        return courseDraftRepository.save(draft);
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureDraftReadyForSubmission(CourseDraft draft) {
        if (!sectionRepository.existsByDraft(draft.getId())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Course must have at least one section before submission");
        }
        if (!lessonRepository.existsByDraft(draft.getId())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Course must have at least one lesson before submission");
        }
    }

    @Override
    @Transactional
    public List<LessonChangedEvent> approveDraft(Course course) {
        CourseDraft draft = findDraft(course).orElse(null);
        if (draft == null) {
            return List.of();
        }
        List<Lesson> draftLessons = lessonRepository.findDraftByDraft(draft.getId());
        List<Lesson> publishedLessons = course.getStatus() == CourseStatus.PUBLISHED
                ? lessonRepository.findPublishedByCourse(course.getId())
                : List.of();
        List<Section> publishedSections = sectionRepository.findPublishedByCourse(course.getId());
        List<LessonChangedEvent> lessonChangedEvents = lessonChangedEvents(course, draft, draftLessons, publishedLessons);
        courseMapper.copyDraftToCourse(draft, course);
        lessonRepository.deleteAll(publishedLessons);
        sectionRepository.deleteAll(publishedSections);
        List<Section> draftSections = sectionRepository.findDraftByDraft(draft.getId());
        draftSections.forEach(section -> {
            section.setCourse(course);
            section.setDraft(null);
        });
        sectionRepository.saveAll(draftSections);
        courseDraftRepository.delete(draft);
        return lessonChangedEvents;
    }

    @Override
    public CourseDraft createDraftFromCourse(Course course) {
        CourseDraft draft = courseMapper.toDraft(course);
        return courseDraftRepository.save(draft);
    }

    private CourseDraft clonePublishedToDraft(Course course) {
        CourseDraft draft = createDraftFromCourse(course);
        Map<String, Section> clonedSections = new HashMap<>();
        List<Section> publishedSections = sectionRepository.findPublishedByCourse(course.getId());
        List<Section> sectionClones = new ArrayList<>();
        for (Section section : publishedSections) {
            Section clone = sectionMapper.cloneSection(section);
            clone.setDraft(draft);
            sectionClones.add(clone);
        }
        List<Section> savedSectionClones = sectionRepository.saveAll(sectionClones);
        for (int i = 0; i < publishedSections.size(); i++) {
            clonedSections.put(publishedSections.get(i).getId(), savedSectionClones.get(i));
        }

        List<Lesson> lessonClones = new ArrayList<>();
        for (Lesson lesson : lessonRepository.findPublishedByCourse(course.getId())) {
            Lesson clone = lessonMapper.cloneLesson(lesson);
            clone.setSection(clonedSections.get(lesson.getSection().getId()));
            clone.setPrerequisiteIds(new ArrayList<>(lesson.getPrerequisiteIds() == null ? List.of() : lesson.getPrerequisiteIds()));
            lessonClones.add(clone);
        }
        lessonRepository.saveAll(lessonClones);
        return draft;
    }

    private List<LessonChangedEvent> lessonChangedEvents(
            Course course,
            CourseDraft draft,
            List<Lesson> draftLessons,
            List<Lesson> publishedLessons
    ) {
        Map<String, Lesson> draftByStableId = draftLessons.stream()
                .collect(HashMap::new, (map, lesson) -> map.put(lesson.getStableId(), lesson), HashMap::putAll);
        Map<String, Lesson> publishedByStableId = publishedLessons.stream()
                .collect(HashMap::new, (map, lesson) -> map.put(lesson.getStableId(), lesson), HashMap::putAll);
        String title = draft.getTitle() != null ? draft.getTitle() : course.getTitle();
        List<LessonChangedEvent> events = new ArrayList<>();

        draftLessons.forEach(lesson -> {
            Lesson published = publishedByStableId.get(lesson.getStableId());
            if (published == null) {
                events.add(new LessonChangedEvent(course.getId(), lesson.getStableId(), "CREATED", title));
            } else if (lessonChanged(published, lesson)) {
                events.add(new LessonChangedEvent(course.getId(), lesson.getStableId(), "UPDATED", title));
            }
        });

        publishedLessons.stream()
                .filter(lesson -> !draftByStableId.containsKey(lesson.getStableId()))
                .forEach(lesson -> events.add(new LessonChangedEvent(course.getId(), lesson.getStableId(), "DELETED", title)));

        return events;
    }

    private boolean lessonChanged(Lesson published, Lesson draft) {
        return !Objects.equals(published.getSection().getStableId(), draft.getSection().getStableId())
                || !Objects.equals(published.getTitle(), draft.getTitle())
                || !Objects.equals(published.getDuration(), draft.getDuration())
                || !Objects.equals(published.getOrderIndex(), draft.getOrderIndex())
                || !Objects.equals(published.getLessonType(), draft.getLessonType())
                || !Objects.equals(published.getIsPreview(), draft.getIsPreview())
                || !Objects.equals(
                        published.getPrerequisiteIds() == null ? List.of() : published.getPrerequisiteIds(),
                        draft.getPrerequisiteIds() == null ? List.of() : draft.getPrerequisiteIds()
                );
    }
}
