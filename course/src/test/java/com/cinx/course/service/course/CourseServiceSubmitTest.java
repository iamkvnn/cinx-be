package com.cinx.course.service.course;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.RejectCourseReasonRepository;
import com.cinx.course.service.curriculum.ICurriculumService;
import com.cinx.course.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceSubmitTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private RejectCourseReasonRepository rejectCourseReasonRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private UserService userService;
    @Mock
    private ICourseAccessService courseAccessService;
    @Mock
    private ICurriculumService curriculumService;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CourseEventProducer courseEventProducer;
    @InjectMocks
    private CourseService courseService;

    @Test
    void submitCourseRejectsDraftWithoutRequiredCurriculum() {
        Course course = course("course-1", "inst-1");
        CourseDraft draft = draft("draft-1", course);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.findDraft(course)).thenReturn(Optional.of(draft));
        doThrow(new BadRequestException("Course must have at least one section before submission"))
                .when(courseDraftService).ensureDraftReadyForSubmission(draft);

        assertThatThrownBy(() -> courseService.submitCourse("inst-1", "course-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Course must have at least one section before submission");

        assertThat(course.getPublishStatus()).isNull();
        verify(courseRepository, never()).save(course);
        verify(courseEventProducer, never()).publishCourseApprovalRequestedEvent(any());
    }

    private Course course(String courseId, String instructorId) {
        Course course = new Course();
        course.setId(courseId);
        course.setInstructorId(instructorId);
        course.setStatus(CourseStatus.DRAFT);
        return course;
    }

    private CourseDraft draft(String draftId, Course course) {
        CourseDraft draft = new CourseDraft();
        draft.setId(draftId);
        draft.setCourse(course);
        return draft;
    }
}
