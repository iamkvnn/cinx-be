package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.mapper.SectionMapper;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.repository.CourseDraftRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseDraftServiceTest {
    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private SectionMapper sectionMapper;
    @Mock
    private LessonMapper lessonMapper;
    @InjectMocks
    private CourseDraftService courseDraftService;

    @Test
    void ensureDraftReadyForSubmissionRejectsDraftWithoutSections() {
        CourseDraft draft = draft("draft-1");
        when(sectionRepository.existsByDraft("draft-1")).thenReturn(false);

        assertThatThrownBy(() -> courseDraftService.ensureDraftReadyForSubmission(draft))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Course must have at least one section before submission");

        verify(lessonRepository, never()).existsByDraft("draft-1");
    }

    @Test
    void ensureDraftReadyForSubmissionRejectsDraftWithoutLessons() {
        CourseDraft draft = draft("draft-1");
        when(sectionRepository.existsByDraft("draft-1")).thenReturn(true);
        when(lessonRepository.existsByDraft("draft-1")).thenReturn(false);

        assertThatThrownBy(() -> courseDraftService.ensureDraftReadyForSubmission(draft))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Course must have at least one lesson before submission");
    }

    @Test
    void ensureDraftReadyForSubmissionAllowsDraftWithSectionAndLesson() {
        CourseDraft draft = draft("draft-1");
        when(sectionRepository.existsByDraft("draft-1")).thenReturn(true);
        when(lessonRepository.existsByDraft("draft-1")).thenReturn(true);

        courseDraftService.ensureDraftReadyForSubmission(draft);
    }

    private CourseDraft draft(String draftId) {
        CourseDraft draft = new CourseDraft();
        draft.setId(draftId);
        return draft;
    }
}
