package com.cinx.course.service.curriculum;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.response.CourseCurriculumResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private LessonMapper lessonMapper;
    @InjectMocks
    private CurriculumService curriculumService;

    @Test
    void getPublishedCurriculumReturnsSectionsWithNestedLessons() {
        Course course = course(true);
        Section firstSection = section("sec-1", 1024);
        Section secondSection = section("sec-2", 2048);
        Lesson firstLesson = lesson("les-1", 1024, firstSection);
        Lesson secondLesson = lesson("les-2", 1024, secondSection);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(sectionRepository.findPublishedByCourse("course-1")).thenReturn(List.of(firstSection, secondSection));
        when(lessonRepository.findPublishedByCourse("course-1")).thenReturn(List.of(firstLesson, secondLesson));
        when(lessonMapper.toResponse(any(Lesson.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        CourseCurriculumResponse response = curriculumService.getPublishedCurriculum("course-1");

        assertThat(response.courseId()).isEqualTo("course-1");
        assertThat(response.sections()).extracting("id").containsExactly("sec-1", "sec-2");
        assertThat(response.sections().get(0).lessons()).extracting(LessonResponse::id).containsExactly("les-1");
        assertThat(response.sections().get(1).lessons()).extracting(LessonResponse::id).containsExactly("les-2");
    }

    @Test
    void getDraftCurriculumUsesDraftWhenPresent() {
        Course course = course(true);
        CourseDraft draft = new CourseDraft();
        draft.setId("draft-1");
        draft.setCourse(course);
        Section section = section("sec-draft", 1024);
        Lesson lesson = lesson("les-draft", 1024, section);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.findDraft(course)).thenReturn(Optional.of(draft));
        when(sectionRepository.findDraftByDraft("draft-1")).thenReturn(List.of(section));
        when(lessonRepository.findDraftByDraft("draft-1")).thenReturn(List.of(lesson));
        when(lessonMapper.toResponse(any(Lesson.class))).thenAnswer(invocation -> response(invocation.getArgument(0)));

        CourseCurriculumResponse response = curriculumService.getDraftCurriculum("course-1");

        assertThat(response.sections()).extracting("id").containsExactly("sec-draft");
        assertThat(response.sections().getFirst().lessons()).extracting(LessonResponse::id).containsExactly("les-draft");
    }

    @Test
    void getPublishedCurriculumRejectsUnpublishedCourse() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course(false)));

        assertThatThrownBy(() -> curriculumService.getPublishedCurriculum("course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    private Course course(boolean published) {
        Course course = new Course();
        course.setId("course-1");
        course.setIsPublished(published);
        return course;
    }

    private Section section(String stableId, Integer orderIndex) {
        Section section = new Section();
        section.setId(stableId + "-entity");
        section.setStableId(stableId);
        section.setTitle(stableId);
        section.setOrderIndex(orderIndex);
        return section;
    }

    private Lesson lesson(String stableId, Integer orderIndex, Section section) {
        Lesson lesson = new Lesson();
        lesson.setId(stableId + "-entity");
        lesson.setStableId(stableId);
        lesson.setTitle(stableId);
        lesson.setLessonType(LessonType.VIDEO);
        lesson.setOrderIndex(orderIndex);
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
