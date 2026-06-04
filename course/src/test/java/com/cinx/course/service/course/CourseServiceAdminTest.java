package com.cinx.course.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.response.CourseCurriculumResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.InstructorCourseSummaryResponse;
import com.cinx.course.dto.response.UserDto;
import com.cinx.course.mapper.CourseMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import com.cinx.course.repository.CategoryRepository;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.RejectCourseReasonRepository;
import com.cinx.course.service.curriculum.ICurriculumService;
import com.cinx.course.service.enrollment.EnrollmentClient;
import com.cinx.course.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceAdminTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private RejectCourseReasonRepository rejectCourseReasonRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ICourseDraftService courseDraftService;
    @Mock
    private EnrollmentClient enrollmentClient;
    @Mock
    private ICurriculumService curriculumService;
    @Mock
    private UserService userService;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CourseEventProducer courseEventProducer;
    @InjectMocks
    private CourseService courseService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getInstructorCourseSummaryReturnsCountsAndAverageRating() {
        when(courseRepository.countByInstructorId("inst-1")).thenReturn(8L);
        when(courseRepository.countByInstructorIdAndStatus("inst-1", CourseStatus.PUBLISHED)).thenReturn(6L);
        when(courseRepository.averageRatingByInstructorId("inst-1")).thenReturn(4.75);

        InstructorCourseSummaryResponse summary = courseService.getInstructorCourseSummary("inst-1");

        assertThat(summary.courseCount()).isEqualTo(8L);
        assertThat(summary.publishedCourseCount()).isEqualTo(6L);
        assertThat(summary.averageRating()).isEqualTo(4.75);
    }

    @Test
    void getPublishedCourseByIdRejectsArchivedCourse() {
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(archivedCourse));

        assertThatThrownBy(() -> courseService.getPublishedCourseById("course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void adminCourseDetailCanReadArchivedCourse() {
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        CourseResponse response = response(archivedCourse);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(archivedCourse));
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(archivedCourse), any(UserDto.class))).thenReturn(response);

        CourseResponse result = courseService.getCourseById("course-1");

        assertThat(result.status()).isEqualTo(CourseStatus.ARCHIVED);
        verify(courseDraftService, never()).findDraft(archivedCourse);
    }

    @Test
    void ownerCanReadArchivedCourseDraftDetailWhenDraftExists() {
        authenticate("inst-1");
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        CourseDraft draft = new CourseDraft();
        draft.setId("draft-1");
        draft.setCourse(archivedCourse);
        CourseResponse response = response(archivedCourse);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(archivedCourse));
        when(courseDraftService.findDraft(archivedCourse)).thenReturn(Optional.of(draft));
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(archivedCourse), eq(draft), any(UserDto.class))).thenReturn(response);

        CourseResponse result = courseService.getOwnedDraftCourseById("course-1");

        assertThat(result.status()).isEqualTo(CourseStatus.ARCHIVED);
    }

    @Test
    void ownerDraftDetailRejectsArchivedCourseWithoutDraft() {
        authenticate("inst-1");
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(archivedCourse));
        when(courseDraftService.findDraft(archivedCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getOwnedDraftCourseById("course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void nonOwnerCannotReadArchivedCourseDraftDetail() {
        authenticate("inst-2");
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(archivedCourse));

        assertThatThrownBy(() -> courseService.getOwnedDraftCourseById("course-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getAllCoursesAllowsArchivedStatusFilter() {
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        CourseResponse response = response(archivedCourse);
        when(courseRepository.searchAll(
                null,
                null,
                "inst-1",
                null,
                null,
                null,
                CourseStatus.ARCHIVED,
                null,
                PageRequest.of(0, 10)
        )).thenReturn(new PageImpl<>(List.of(archivedCourse)));
        when(userService.getInstructorsByIds(List.of("inst-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(instructor("inst-1"))));
        when(courseMapper.toResponse(eq(archivedCourse), any(UserDto.class))).thenReturn(response);

        var result = courseService.getAllCourses(
                null,
                null,
                "inst-1",
                null,
                null,
                null,
                CourseStatus.ARCHIVED,
                null,
                1,
                10,
                null
        );

        assertThat(result.getContent()).extracting(CourseResponse::status).containsExactly(CourseStatus.ARCHIVED);
    }

    @Test
    void getPublishedSnapshotIgnoresDraftWhenDraftExists() {
        Course course = course("course-1", "inst-1", CourseStatus.PUBLISHED);
        CourseDraft draft = new CourseDraft();
        draft.setId("draft-1");
        draft.setCourse(course);
        CourseResponse publishedResponse = response(course);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(course), any(UserDto.class))).thenReturn(publishedResponse);

        CourseResponse result = courseService.getPublishedSnapshotCourseById("course-1");

        assertThat(result).isSameAs(publishedResponse);
        verify(courseDraftService, never()).findDraft(course);
    }

    @Test
    void adminCourseDetailDoesNotReturnDraftWhenDraftExists() {
        Course course = course("course-1", "inst-1", CourseStatus.PUBLISHED);
        CourseDraft draft = new CourseDraft();
        draft.setId("draft-1");
        draft.setCourse(course);
        CourseResponse publishedResponse = response(course);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(course), any(UserDto.class))).thenReturn(publishedResponse);

        CourseResponse result = courseService.getCourseById("course-1");

        assertThat(result).isSameAs(publishedResponse);
        verify(courseDraftService, never()).findDraft(course);
    }

    @Test
    void getDraftCourseReturnsCourseRowForFirstTimeDraft() {
        Course course = course("course-1", "inst-1", CourseStatus.DRAFT);
        CourseResponse response = response(course);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.findDraft(course)).thenReturn(Optional.empty());
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(course), any(UserDto.class))).thenReturn(response);

        CourseResponse result = courseService.getDraftCourseById("course-1");

        assertThat(result).isSameAs(response);
    }

    @Test
    void getDraftCourseReturnsCourseDraftWhenDraftExists() {
        Course course = course("course-1", "inst-1", CourseStatus.PUBLISHED);
        CourseDraft draft = new CourseDraft();
        draft.setId("draft-1");
        draft.setCourse(course);
        CourseResponse draftResponse = response(course);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.findDraft(course)).thenReturn(Optional.of(draft));
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(course), eq(draft), any(UserDto.class))).thenReturn(draftResponse);

        CourseResponse result = courseService.getDraftCourseById("course-1");

        assertThat(result).isSameAs(draftResponse);
    }

    @Test
    void getDraftCourseRejectsPublishedCourseWithoutDraft() {
        Course course = course("course-1", "inst-1", CourseStatus.PUBLISHED);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseDraftService.findDraft(course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getDraftCourseById("course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPublishedSnapshotRejectsFirstTimeDraft() {
        Course course = course("course-1", "inst-1", CourseStatus.DRAFT);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.getPublishedSnapshotCourseById("course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void unarchiveRestoresArchivedCourseAndKeepsDraft() {
        authenticate("inst-1");
        Course archivedCourse = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        archivedCourse.setPublishStatus(CoursePublishStatus.REJECTED);
        CourseResponse response = response(archivedCourse);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(archivedCourse));
        when(courseRepository.save(archivedCourse)).thenReturn(archivedCourse);
        when(userService.getInstructorById("inst-1")).thenReturn(new ApiResponse<>(true, "ok", instructor("inst-1")));
        when(courseMapper.toResponse(eq(archivedCourse), any(UserDto.class))).thenReturn(response);
        when(curriculumService.getEnrolledCurriculum("course-1")).thenReturn(new CourseCurriculumResponse("course-1", List.of()));

        CourseResponse result = courseService.unarchiveCourse("course-1");

        assertThat(archivedCourse.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(archivedCourse.getPublishStatus()).isNull();
        assertThat(result).isSameAs(response);
        verify(courseDraftService, never()).findDraft(archivedCourse);
    }

    @Test
    void unarchiveRejectsNonArchivedCourse() {
        authenticate("inst-1");
        Course course = course("course-1", "inst-1", CourseStatus.PUBLISHED);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.unarchiveCourse("course-1"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void approveCourseRejectsArchivedCourse() {
        Course course = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        course.setPublishStatus(CoursePublishStatus.WAITING_APPROVAL);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.approveCourse("course-1"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectCourseRejectsArchivedCourse() {
        Course course = course("course-1", "inst-1", CourseStatus.ARCHIVED);
        course.setPublishStatus(CoursePublishStatus.WAITING_APPROVAL);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.rejectCourse("course-1", new RejectCourseRequest("Needs changes")))
                .isInstanceOf(BadRequestException.class);
    }

    private void authenticate(String userId) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userId, null));
    }

    private Course course(String courseId, String instructorId, CourseStatus status) {
        Course course = new Course();
        course.setId(courseId);
        course.setInstructorId(instructorId);
        course.setStatus(status);
        course.setPublishStatus(status == CourseStatus.PUBLISHED ? CoursePublishStatus.PUBLISHED : null);
        return course;
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
                null,
                null,
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
