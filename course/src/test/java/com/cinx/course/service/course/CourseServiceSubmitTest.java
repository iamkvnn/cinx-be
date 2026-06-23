package com.cinx.course.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.UserDto;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.model.Category;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void createCourseKeepsRatingNullUntilFirstReview() {
        CreateCourseRequest request = new CreateCourseRequest(
                "Spring Boot Mastery",
                "Learn Spring Boot from basics to production",
                "cat-1",
                100_000L,
                null,
                false,
                120L,
                false,
                null
        );
        Category category = new Category();
        category.setId("cat-1");
        Course mappedCourse = new Course();
        CourseDraft draft = new CourseDraft();
        CourseResponse response = response(mappedCourse);
        when(courseMapper.toModel(request)).thenReturn(mappedCourse);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(courseRepository.save(mappedCourse)).thenReturn(mappedCourse);
        when(courseDraftService.createDraftFromCourse(mappedCourse)).thenReturn(draft);
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(mappedCourse), eq(draft), any(UserDto.class))).thenReturn(response);

        courseService.createCourse("inst-1", request);

        assertThat(mappedCourse.getInstructorId()).isEqualTo("inst-1");
        assertThat(mappedCourse.getEnrollmentCount()).isZero();
        assertThat(mappedCourse.getRating()).isNull();
        assertThat(mappedCourse.getStatus()).isEqualTo(CourseStatus.DRAFT);
        verify(courseRepository).save(mappedCourse);
    }

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

    @Test
    void updatePublishedCourseUpdatesDraftOnlyUntilApproval() {
        Course course = course("course-1", "inst-1");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishStatus(CoursePublishStatus.PUBLISHED);
        course.setTitle("Published title");
        course.setPrice(100_000L);
        CourseDraft draft = draft("draft-1", course);
        UpdateCourseRequest request = new UpdateCourseRequest(
                "Draft title",
                "Draft description",
                "cat-2",
                120_000L,
                90_000L,
                true,
                150L,
                true,
                "Draft certificate"
        );
        Category category = new Category();
        category.setId("cat-2");
        CourseResponse response = response(course);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(categoryRepository.findById("cat-2")).thenReturn(Optional.of(category));
        when(courseDraftService.updateDraft(course, request, category, 25L)).thenReturn(draft);
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(course), eq(draft), any(UserDto.class))).thenReturn(response);

        CourseResponse result = courseService.updateCourse("inst-1", "course-1", request);

        assertThat(result).isSameAs(response);
        assertThat(course.getTitle()).isEqualTo("Published title");
        assertThat(course.getPrice()).isEqualTo(100_000L);
        assertThat(course.getPublishStatus()).isEqualTo(CoursePublishStatus.PUBLISHED);
        verify(courseMapper, never()).partialUpdate(course, request);
        verify(courseRepository, never()).save(course);
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

    private UserDto instructor(String instructorId) {
        return new UserDto(instructorId, "Instructor", "instructor@example.com", null, null);
    }

    private CourseResponse response(Course course) {
        return new CourseResponse(
                course.getId(),
                null,
                null,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                course.getRating(),
                course.getEnrollmentCount(),
                null,
                null,
                null,
                null,
                course.getStatus(),
                course.getPublishStatus(),
                null,
                null
        );
    }
}
