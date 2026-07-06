package com.cinx.course.service.section;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.dto.request.MoveSectionRequest;
import com.cinx.course.dto.response.SectionPositionResponse;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.course.ICourseDraftService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SectionMapper sectionMapper;
    @InjectMocks
    private SectionService sectionService;

    @Test
    void moveSection_updatesOnlyMovedSectionWhenSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1024, draft);
        Section second = section("sec-2", 2048, draft);
        Section third = section("sec-3", 3072, draft);
        mockDraft(course, draft, List.of(first, second, third));

        SectionPositionResponse response = sectionService.moveSection(
                "course-1",
                "sec-2",
                new MoveSectionRequest("sec-3", null)
        );

        Section saved = capturedSavedSection();
        assertThat(saved).isSameAs(second);
        assertThat(second.getOrderIndex()).isEqualTo(4096);
        assertThat(response.sectionId()).isEqualTo("sec-2");
        assertThat(response.orderIndex()).isEqualTo(4096);
        verify(sectionRepository, never()).saveAll(any());
    }

    @Test
    void moveSection_movesToStart() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1024, draft);
        Section second = section("sec-2", 2048, draft);
        Section third = section("sec-3", 3072, draft);
        mockDraft(course, draft, List.of(first, second, third));

        sectionService.moveSection("course-1", "sec-3", new MoveSectionRequest(null, "sec-1"));

        assertThat(capturedSavedSection()).isSameAs(third);
        assertThat(third.getOrderIndex()).isEqualTo(512);
        verify(sectionRepository, never()).saveAll(any());
    }

    @Test
    void moveSection_noopsWhenPositionDoesNotChange() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1024, draft);
        Section second = section("sec-2", 2048, draft);
        Section third = section("sec-3", 3072, draft);
        mockDraft(course, draft, List.of(first, second, third));

        SectionPositionResponse response = sectionService.moveSection(
                "course-1",
                "sec-2",
                new MoveSectionRequest("sec-1", "sec-3")
        );

        assertThat(response.orderIndex()).isEqualTo(2048);
        verify(sectionRepository, never()).save(any());
        verify(sectionRepository, never()).saveAll(any());
    }

    @Test
    void moveSection_rebalancesWhenNoSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1, draft);
        Section second = section("sec-2", 2, draft);
        Section third = section("sec-3", 3, draft);
        mockDraft(course, draft, List.of(first, second, third));

        sectionService.moveSection("course-1", "sec-2", new MoveSectionRequest(null, "sec-1"));

        assertThat(capturedSavedSections()).containsExactly(second, first, third);
        assertThat(second.getOrderIndex()).isEqualTo(1024);
        assertThat(first.getOrderIndex()).isEqualTo(2048);
        assertThat(third.getOrderIndex()).isEqualTo(3072);
        verify(sectionRepository, never()).save(any());
    }

    @Test
    void moveSection_rejectsInvalidNeighbors() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1024, draft);
        Section second = section("sec-2", 2048, draft);
        mockDraft(course, draft, List.of(first, second));

        assertThatThrownBy(() -> sectionService.moveSection(
                "course-1",
                "sec-2",
                new MoveSectionRequest("sec-2", null)
        )).isInstanceOf(BadRequestException.class);

        verify(sectionRepository, never()).save(any());
        verify(sectionRepository, never()).saveAll(any());
    }

    @Test
    void deleteSection_deletesLessonsBeforeSection() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section section = section("sec-1", 1024, draft);
        Lesson lesson = new Lesson();
        lesson.setId("lesson-1");
        lesson.setSection(section);
        section.getLessons().add(lesson);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftSection("draft-1", "sec-1")).thenReturn(Optional.of(section));
        when(lessonRepository.findBySectionIdsForUpdate(List.of("sec-1-entity"))).thenReturn(List.of(lesson));

        sectionService.deleteSection("course-1", "sec-1");

        InOrder inOrder = inOrder(lessonRepository, sectionRepository);
        inOrder.verify(lessonRepository).findBySectionIdsForUpdate(List.of("sec-1-entity"));
        inOrder.verify(lessonRepository).deleteAll(List.of(lesson));
        inOrder.verify(sectionRepository).delete(section);
        assertThat(section.getLessons()).isEmpty();
    }

    private void mockDraft(Course course, CourseDraft draft, List<Section> sections) {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftByDraftForUpdate("draft-1")).thenReturn(sections);
    }

    private Section capturedSavedSection() {
        ArgumentCaptor<Section> captor = ArgumentCaptor.forClass(Section.class);
        verify(sectionRepository).save(captor.capture());
        return captor.getValue();
    }

    private List<Section> capturedSavedSections() {
        ArgumentCaptor<Iterable<Section>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(sectionRepository).saveAll(captor.capture());
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

    private Section section(String stableId, Integer orderIndex, CourseDraft draft) {
        Section section = new Section();
        section.setId(stableId + "-entity");
        section.setStableId(stableId);
        section.setOrderIndex(orderIndex);
        section.setDraft(draft);
        return section;
    }
}
