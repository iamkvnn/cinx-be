package com.cinx.course.service.lesson;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.ReorderLessonsRequest;
import com.cinx.course.dto.request.SectionLessonsOrderRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private ISectionService sectionService;
    @Mock
    private LessonMapper lessonMapper;
    @InjectMocks
    private LessonService lessonService;

    @Test
    void reorderLessons_updatesOnlyMovedLessonInsideSectionWhenSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        Lesson first = lesson("les-1", 1024, section);
        Lesson second = lesson("les-2", 2048, section);
        Lesson third = lesson("les-3", 3072, section);
        mockDraft(course, draft, List.of(section), List.of(first, second, third));
        when(lessonMapper.toResponse(any(Lesson.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        lessonService.reorderLessons("course-1", new ReorderLessonsRequest(List.of(
                new SectionLessonsOrderRequest("sec-1", List.of("les-1", "les-3", "les-2"))
        )));

        List<Lesson> saved = capturedSavedLessons();
        assertThat(saved).containsExactly(second);
        assertThat(second.getSection()).isSameAs(section);
        assertThat(second.getOrderIndex()).isEqualTo(4096);
    }

    @Test
    void reorderLessons_movesLessonAcrossSections() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section firstSection = section("sec-1", draft);
        Section secondSection = section("sec-2", draft);
        Lesson first = lesson("les-1", 1024, firstSection);
        Lesson second = lesson("les-2", 2048, firstSection);
        Lesson third = lesson("les-3", 1024, secondSection);
        mockDraft(course, draft, List.of(firstSection, secondSection), List.of(first, second, third));
        when(lessonMapper.toResponse(any(Lesson.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        lessonService.reorderLessons("course-1", new ReorderLessonsRequest(List.of(
                new SectionLessonsOrderRequest("sec-1", List.of("les-1")),
                new SectionLessonsOrderRequest("sec-2", List.of("les-3", "les-2"))
        )));

        List<Lesson> saved = capturedSavedLessons();
        assertThat(saved).containsExactly(second);
        assertThat(second.getSection()).isSameAs(secondSection);
        assertThat(second.getOrderIndex()).isEqualTo(2048);
    }

    @Test
    void reorderLessons_rejectsDuplicateLessonIds() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        Lesson first = lesson("les-1", 1024, section);
        Lesson second = lesson("les-2", 2048, section);
        mockDraft(course, draft, List.of(section), List.of(first, second));

        ReorderLessonsRequest request = new ReorderLessonsRequest(List.of(
                new SectionLessonsOrderRequest("sec-1", List.of("les-1", "les-1"))
        ));

        assertThatThrownBy(() -> lessonService.reorderLessons("course-1", request))
                .isInstanceOf(BadRequestException.class);

        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void updateLesson_acceptsPrerequisiteFromAnotherSectionInSameDraft() {
        Section firstSection = section("sec-1", draft(course()));
        Section secondSection = section("sec-2", firstSection.getDraft());
        Lesson first = lesson("les-1", 1024, firstSection);
        Lesson second = lesson("les-2", 1024, secondSection);
        when(sectionService.editableSection("course-1", "sec-1")).thenReturn(firstSection);
        when(lessonRepository.findBySectionAndStableId(firstSection.getId(), "les-1")).thenReturn(Optional.of(first));
        when(lessonRepository.findByDraft("draft-1")).thenReturn(List.of(first, second));
        when(lessonMapper.toResponse(first)).thenReturn(response(first));

        lessonService.updateLesson("course-1", "sec-1", "les-1", new UpdateLessonRequest(
                null,
                null,
                null,
                List.of("les-2")
        ));

        assertThat(first.getPrerequisiteIds()).containsExactly("les-2");
        verify(lessonRepository).save(first);
    }

    @Test
    void updateLesson_rejectsCircularPrerequisitesAcrossSections() {
        Section firstSection = section("sec-1", draft(course()));
        Section secondSection = section("sec-2", firstSection.getDraft());
        Lesson first = lesson("les-1", 1024, firstSection);
        Lesson second = lesson("les-2", 1024, secondSection);
        second.setPrerequisiteIds(List.of("les-1"));
        when(sectionService.editableSection("course-1", "sec-1")).thenReturn(firstSection);
        when(lessonRepository.findBySectionAndStableId(firstSection.getId(), "les-1")).thenReturn(Optional.of(first));
        when(lessonRepository.findByDraft("draft-1")).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> lessonService.updateLesson("course-1", "sec-1", "les-1", new UpdateLessonRequest(
                null,
                null,
                null,
                List.of("les-2")
        ))).isInstanceOf(BadRequestException.class);
    }

    private void mockDraft(Course course, CourseDraft draft, List<Section> sections, List<Lesson> lessons) {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftByDraftForUpdate("draft-1")).thenReturn(sections);
        when(lessonRepository.findBySectionIdsForUpdate(any())).thenReturn(lessons);
    }

    private List<Lesson> capturedSavedLessons() {
        ArgumentCaptor<Iterable<Lesson>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(lessonRepository).saveAll(captor.capture());
        return StreamSupport.stream(captor.getValue().spliterator(), false).toList();
    }

    private Course course() {
        Course course = new Course();
        course.setId("course-1");
        return course;
    }

    private CourseDraft draft(Course course) {
        CourseDraft draft = new CourseDraft();
        draft.setId("draft-1");
        draft.setCourse(course);
        return draft;
    }

    private Section section(String stableId, CourseDraft draft) {
        Section section = new Section();
        section.setId(stableId + "-entity");
        section.setStableId(stableId);
        section.setDraft(draft);
        return section;
    }

    private Lesson lesson(String stableId, Integer orderIndex, Section section) {
        Lesson lesson = new Lesson();
        lesson.setId(stableId + "-entity");
        lesson.setStableId(stableId);
        lesson.setOrderIndex(orderIndex);
        lesson.setLessonType(LessonType.VIDEO);
        lesson.setSection(section);
        lesson.setPrerequisiteIds(List.of());
        return lesson;
    }

    private LessonResponse response(Lesson lesson) {
        return new LessonResponse(
                lesson.getStableId(),
                lesson.getTitle(),
                lesson.getDuration(),
                lesson.getLessonType(),
                lesson.getOrderIndex(),
                lesson.getIsPreview(),
                lesson.getPrerequisiteIds(),
                null,
                null
        );
    }
}
