package com.cinx.course.service.change;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.response.CourseChangeResponse;
import com.cinx.course.model.*;
import com.cinx.course.repository.CourseDraftRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.utils.JsonConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseChangeAuditService implements ICourseChangeAuditService {
    private static final String COURSE = "COURSE";
    private static final String SECTION = "SECTION";
    private static final String LESSON = "LESSON";

    private final CourseRepository courseRepository;
    private final CourseDraftRepository courseDraftRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final JsonConverter jsonConverter;

    @Override
    @Transactional(readOnly = true)
    public List<CourseChangeResponse> getCourseChangeHistory(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        Optional<CourseDraft> draft = courseDraftRepository.findByCourse(courseId);
        if (draft.isEmpty() && Boolean.TRUE.equals(course.getIsPublished())) {
            return List.of();
        }

        List<CourseChangeResponse> changes = new ArrayList<>();
        addCourseChange(changes, course, draft.orElse(null));
        addSectionChanges(changes, course, draft.orElse(null));
        addLessonChanges(changes, course, draft.orElse(null));
        return changes;
    }

    private void addCourseChange(List<CourseChangeResponse> changes, Course course, CourseDraft draft) {
        Map<String, Object> oldValue = Boolean.TRUE.equals(course.getIsPublished()) ? courseValue(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getPrice(),
                course.getDiscountedPrice(),
                course.getDiscountRate(),
                course.getIsInSubscription(),
                course.getDuration(),
                course.getHasCertificate(),
                course.getCertificateTitle()
        ) : null;
        Map<String, Object> newValue = draft == null ? courseValue(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getPrice(),
                course.getDiscountedPrice(),
                course.getDiscountRate(),
                course.getIsInSubscription(),
                course.getDuration(),
                course.getHasCertificate(),
                course.getCertificateTitle()
        ) : courseValue(
                course.getId(),
                draft.getTitle(),
                draft.getDescription(),
                draft.getCategory(),
                draft.getPrice(),
                draft.getDiscountedPrice(),
                draft.getDiscountRate(),
                draft.getIsInSubscription(),
                draft.getDuration(),
                draft.getHasCertificate(),
                draft.getCertificateTitle()
        );
        addChangeIfNeeded(changes, course.getId(), null, COURSE, oldValue, newValue);
    }

    private void addSectionChanges(List<CourseChangeResponse> changes, Course course, CourseDraft draft) {
        List<Section> currentSections = draft == null
                ? sectionRepository.findPublishedByCourse(course.getId())
                : sectionRepository.findDraftByDraft(draft.getId());
        Map<String, Section> currentById = currentSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));

        List<Section> publishedSections = Boolean.TRUE.equals(course.getIsPublished())
                ? sectionRepository.findPublishedByCourse(course.getId())
                : List.of();
        Map<String, Section> publishedById = publishedSections.stream()
                .collect(Collectors.toMap(Section::getStableId, Function.identity()));

        currentSections.forEach(section -> addChangeIfNeeded(
                changes,
                course.getId(),
                section.getStableId(),
                SECTION,
                Optional.ofNullable(publishedById.get(section.getStableId())).map(this::sectionValue).orElse(null),
                sectionValue(section)
        ));

        publishedSections.stream()
                .filter(section -> !currentById.containsKey(section.getStableId()))
                .forEach(section -> addChangeIfNeeded(
                        changes,
                        course.getId(),
                        section.getStableId(),
                        SECTION,
                        sectionValue(section),
                        null
                ));
    }

    private void addLessonChanges(List<CourseChangeResponse> changes, Course course, CourseDraft draft) {
        List<Lesson> currentLessons = currentLessons(course, draft);
        Map<String, Lesson> currentById = currentLessons.stream()
                .collect(Collectors.toMap(Lesson::getStableId, Function.identity()));

        List<Lesson> publishedLessons = Boolean.TRUE.equals(course.getIsPublished())
                ? lessonRepository.findPublishedByCourse(course.getId())
                : List.of();
        Map<String, Lesson> publishedById = publishedLessons.stream()
                .collect(Collectors.toMap(Lesson::getStableId, Function.identity()));

        currentLessons.forEach(lesson -> addChangeIfNeeded(
                changes,
                course.getId(),
                lesson.getStableId(),
                LESSON,
                Optional.ofNullable(publishedById.get(lesson.getStableId())).map(this::lessonValue).orElse(null),
                lessonValue(lesson)
        ));

        publishedLessons.stream()
                .filter(lesson -> !currentById.containsKey(lesson.getStableId()))
                .forEach(lesson -> addChangeIfNeeded(
                        changes,
                        course.getId(),
                        lesson.getStableId(),
                        LESSON,
                        lessonValue(lesson),
                        null
                ));
    }

    private List<Lesson> currentLessons(Course course, CourseDraft draft) {
        if (draft == null) {
            return lessonRepository.findPublishedByCourse(course.getId());
        }
        return lessonRepository.findDraftByDraft(draft.getId());
    }

    private void addChangeIfNeeded(
            List<CourseChangeResponse> changes,
            String courseId,
            String itemId,
            String itemType,
            Map<String, Object> oldValue,
            Map<String, Object> newValue
    ) {
        String oldJson = oldValue == null ? null : jsonConverter.toJson(oldValue);
        String newJson = newValue == null ? null : jsonConverter.toJson(newValue);
        if (Objects.equals(oldJson, newJson)) {
            return;
        }
        changes.add(new CourseChangeResponse(
                courseId,
                itemId,
                itemType,
                oldJson,
                newJson
        ));
    }

    private Map<String, Object> courseValue(
            String id,
            String title,
            String description,
            Category category,
            Long price,
            Long discountedPrice,
            Long discountRate,
            Boolean isInSubscription,
            Long duration,
            Boolean hasCertificate,
            String certificateTitle
    ) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", id);
        value.put("title", title);
        value.put("description", description);
        value.put("categoryId", category != null ? category.getId() : null);
        value.put("categoryName", category != null ? category.getName() : null);
        value.put("price", price);
        value.put("discountedPrice", discountedPrice);
        value.put("discountRate", discountRate);
        value.put("isInSubscription", isInSubscription);
        value.put("duration", duration);
        value.put("hasCertificate", hasCertificate);
        value.put("certificateTitle", certificateTitle);
        return value;
    }

    private Map<String, Object> sectionValue(Section section) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", section.getStableId());
        value.put("title", section.getTitle());
        value.put("description", section.getDescription());
        value.put("duration", section.getDuration());
        value.put("orderIndex", section.getOrderIndex());
        return value;
    }

    private Map<String, Object> lessonValue(Lesson lesson) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", lesson.getStableId());
        value.put("sectionId", lesson.getSection().getStableId());
        value.put("title", lesson.getTitle());
        value.put("duration", lesson.getDuration());
        value.put("orderIndex", lesson.getOrderIndex());
        value.put("lessonType", lesson.getLessonType());
        value.put("isPreview", lesson.getIsPreview());
        value.put("prerequisiteIds", lesson.getPrerequisiteIds() == null ? List.of() : List.copyOf(lesson.getPrerequisiteIds()));
        return value;
    }
}
