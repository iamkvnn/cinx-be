package com.cinx.course.service.lesson;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.consts.LessonType;
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
    void moveLesson_updatesOnlyMovedLessonInsideSectionWhenSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        Lesson first = lesson("les-1", 1024, section);
        Lesson second = lesson("les-2", 2048, section);
        Lesson third = lesson("les-3", 3072, section);
        mockDraft(course, draft, List.of(section), second, List.of(first, second, third));

        LessonPositionResponse response = lessonService.moveLesson(
                "course-1",
                "les-2",
                new MoveLessonRequest("sec-1", "les-3", null)
        );

        Lesson saved = capturedSavedLesson();
        assertThat(saved).isSameAs(second);
        assertThat(second.getSection()).isSameAs(section);
        assertThat(second.getOrderIndex()).isEqualTo(4096);
        assertThat(response.lessonId()).isEqualTo("les-2");
        assertThat(response.sectionId()).isEqualTo("sec-1");
        assertThat(response.orderIndex()).isEqualTo(4096);
        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void moveLesson_movesLessonAcrossSections() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section firstSection = section("sec-1", draft);
        Section secondSection = section("sec-2", draft);
        Lesson first = lesson("les-1", 1024, firstSection);
        Lesson second = lesson("les-2", 2048, firstSection);
        Lesson third = lesson("les-3", 1024, secondSection);
        mockDraft(course, draft, List.of(firstSection, secondSection), second, List.of(first, second, third));

        lessonService.moveLesson("course-1", "les-2", new MoveLessonRequest("sec-2", "les-3", null));

        Lesson saved = capturedSavedLesson();
        assertThat(saved).isSameAs(second);
        assertThat(second.getSection()).isSameAs(secondSection);
        assertThat(second.getOrderIndex()).isEqualTo(2048);
        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void moveLesson_movesLessonToEmptySection() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section firstSection = section("sec-1", draft);
        Section secondSection = section("sec-2", draft);
        Lesson lesson = lesson("les-1", 2048, firstSection);
        mockDraft(course, draft, List.of(firstSection, secondSection), lesson, List.of(lesson));

        lessonService.moveLesson("course-1", "les-1", new MoveLessonRequest("sec-2", null, null));

        Lesson saved = capturedSavedLesson();
        assertThat(saved).isSameAs(lesson);
        assertThat(lesson.getSection()).isSameAs(secondSection);
        assertThat(lesson.getOrderIndex()).isEqualTo(1024);
        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void moveLesson_noopsWhenPositionDoesNotChange() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        Lesson first = lesson("les-1", 1024, section);
        Lesson second = lesson("les-2", 2048, section);
        Lesson third = lesson("les-3", 3072, section);
        mockDraft(course, draft, List.of(section), second, List.of(first, second, third));

        LessonPositionResponse response = lessonService.moveLesson(
                "course-1",
                "les-2",
                new MoveLessonRequest("sec-1", "les-1", "les-3")
        );

        assertThat(response.orderIndex()).isEqualTo(2048);
        verify(lessonRepository, never()).save(any());
        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void moveLesson_rebalancesWhenNoSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        Lesson first = lesson("les-1", 1, section);
        Lesson second = lesson("les-2", 2, section);
        Lesson third = lesson("les-3", 3, section);
        mockDraft(course, draft, List.of(section), second, List.of(first, second, third));

        lessonService.moveLesson("course-1", "les-2", new MoveLessonRequest("sec-1", null, "les-1"));

        assertThat(capturedSavedLessons()).containsExactly(second, first, third);
        assertThat(second.getOrderIndex()).isEqualTo(1024);
        assertThat(first.getOrderIndex()).isEqualTo(2048);
        assertThat(third.getOrderIndex()).isEqualTo(3072);
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void moveLesson_rejectsTargetSectionOutsideDraft() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        mockDraftSections(course, draft, List.of(section));

        assertThatThrownBy(() -> lessonService.moveLesson(
                "course-1",
                "les-1",
                new MoveLessonRequest("sec-missing", null, null)
        )).isInstanceOf(BadRequestException.class);

        verify(lessonRepository, never()).findDraftLessonForUpdate(any(), any());
        verify(lessonRepository, never()).save(any());
        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void moveLesson_rejectsNeighborOutsideTargetSection() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section firstSection = section("sec-1", draft);
        Section secondSection = section("sec-2", draft);
        Lesson first = lesson("les-1", 1024, firstSection);
        Lesson second = lesson("les-2", 2048, firstSection);
        Lesson third = lesson("les-3", 1024, secondSection);
        mockDraft(course, draft, List.of(firstSection, secondSection), second, List.of(first, second, third));

        assertThatThrownBy(() -> lessonService.moveLesson(
                "course-1",
                "les-2",
                new MoveLessonRequest("sec-2", "les-1", null)
        )).isInstanceOf(BadRequestException.class);

        verify(lessonRepository, never()).save(any());
        verify(lessonRepository, never()).saveAll(any());
    }

    @Test
    void moveLesson_rejectsMovedLessonAsNeighbor() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", draft);
        Lesson first = lesson("les-1", 1024, section);
        Lesson second = lesson("les-2", 2048, section);
        mockDraft(course, draft, List.of(section), second, List.of(first, second));

        assertThatThrownBy(() -> lessonService.moveLesson(
                "course-1",
                "les-2",
                new MoveLessonRequest("sec-1", "les-2", null)
        )).isInstanceOf(BadRequestException.class);

        verify(lessonRepository, never()).save(any());
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

    private void mockDraft(
            Course course,
            CourseDraft draft,
            List<Section> sections,
            Lesson movedLesson,
            List<Lesson> affectedLessons
    ) {
        mockDraftSections(course, draft, sections);
        when(lessonRepository.findDraftLessonForUpdate("draft-1", movedLesson.getStableId())).thenReturn(Optional.of(movedLesson));
        when(lessonRepository.findBySectionIdsForUpdate(any())).thenReturn(affectedLessons);
    }

    private void mockDraftSections(Course course, CourseDraft draft, List<Section> sections) {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftByDraftForUpdate("draft-1")).thenReturn(sections);
    }

    private Lesson capturedSavedLesson() {
        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        return captor.getValue();
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
