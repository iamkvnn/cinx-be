package com.cinx.course.service.section;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.course.ICourseDraftService;
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
class SectionServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SectionMapper sectionMapper;
    @InjectMocks
    private SectionService sectionService;

    @Test
    void reorderSections_updatesOnlyMovedSectionWhenSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1024, draft);
        Section second = section("sec-2", 2048, draft);
        Section third = section("sec-3", 3072, draft);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftByDraftForUpdate("draft-1")).thenReturn(List.of(first, second, third));
        when(sectionMapper.toResponse(any(Section.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        List<SectionResponse> response = sectionService.reorderSections(
                "course-1",
                List.of("sec-1", "sec-3", "sec-2")
        );

        List<Section> saved = capturedSavedSections();
        assertThat(saved).containsExactly(second);
        assertThat(second.getOrderIndex()).isEqualTo(4096);
        assertThat(response).extracting(SectionResponse::id).containsExactly("sec-1", "sec-3", "sec-2");
    }

    @Test
    void reorderSections_rebalancesWhenNoSparseGapExists() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1, draft);
        Section second = section("sec-2", 2, draft);
        Section third = section("sec-3", 3, draft);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftByDraftForUpdate("draft-1")).thenReturn(List.of(first, second, third));
        when(sectionMapper.toResponse(any(Section.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        sectionService.reorderSections("course-1", List.of("sec-2", "sec-1", "sec-3"));

        assertThat(capturedSavedSections()).containsExactly(second, first, third);
        assertThat(second.getOrderIndex()).isEqualTo(1024);
        assertThat(first.getOrderIndex()).isEqualTo(2048);
        assertThat(third.getOrderIndex()).isEqualTo(3072);
    }

    @Test
    void reorderSections_rejectsDuplicateOrMissingSections() {
        Course course = course();
        CourseDraft draft = draft(course);
        Section first = section("sec-1", 1024, draft);
        Section second = section("sec-2", 2048, draft);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.getOrCreateDraft(course)).thenReturn(draft);
        when(sectionRepository.findDraftByDraftForUpdate("draft-1")).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> sectionService.reorderSections(
                "course-1",
                List.of("sec-1", "sec-1")
        )).isInstanceOf(BadRequestException.class);

        verify(sectionRepository, never()).saveAll(any());
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

    private SectionResponse response(Section section) {
        return new SectionResponse(
                section.getStableId(),
                section.getTitle(),
                section.getDescription(),
                section.getDuration(),
                section.getOrderIndex()
        );
    }
}
