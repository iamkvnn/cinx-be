package com.cinx.course.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.response.CheckEnrollmentStatus;
import com.cinx.course.model.Course;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.service.enrollment.EnrollmentClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseAccessServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EnrollmentClient enrollmentClient;
    @InjectMocks
    private CourseAccessService courseAccessService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ensureReadableCourseAllowsPublishedCourseForAnonymousUser() {
        Course course = course(CourseStatus.PUBLISHED, "inst-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        Course result = courseAccessService.ensureReadableCourse(null, "course-1");

        assertThat(result).isSameAs(course);
        verify(enrollmentClient, never()).checkEnrollmentStatus("anonymous", List.of("course-1"));
    }

    @Test
    void ensureReadableCourseRejectsDraftCourse() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course(CourseStatus.DRAFT, "inst-1")));

        assertThatThrownBy(() -> courseAccessService.ensureReadableCourse(null, "course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void ensureReadableCourseRejectsArchivedCourseForAnonymousUser() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course(CourseStatus.ARCHIVED, "inst-1")));

        assertThatThrownBy(() -> courseAccessService.ensureReadableCourse(null, "course-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void canReadCourseAllowsArchivedCourseForOwner() {
        Course course = course(CourseStatus.ARCHIVED, "inst-1");

        assertThat(courseAccessService.canReadCourse("inst-1", course)).isTrue();
    }

    @Test
    void canReadCourseAllowsArchivedCourseForAdmin() {
        authenticateAdmin("admin-1");
        Course course = course(CourseStatus.ARCHIVED, "inst-1");

        assertThat(courseAccessService.canReadCourse("admin-1", course)).isTrue();
    }

    @Test
    void canReadCourseAllowsArchivedCourseForEnrolledUser() {
        Course course = course(CourseStatus.ARCHIVED, "inst-1");
        when(enrollmentClient.checkEnrollmentStatus("student-1", List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));

        assertThat(courseAccessService.canReadCourse("student-1", course)).isTrue();
    }

    @Test
    void enrollmentByCourseIdBatchesOnlyArchivedNonOwnerCourses() {
        Course published = course("published", CourseStatus.PUBLISHED, "inst-1");
        Course ownedArchived = course("owned", CourseStatus.ARCHIVED, "student-1");
        Course archived = course("archived", CourseStatus.ARCHIVED, "inst-1");
        when(enrollmentClient.checkEnrollmentStatus("student-1", List.of("archived")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("archived", true))));

        Map<String, Boolean> enrollmentByCourseId = courseAccessService.enrollmentByCourseId(
                "student-1",
                List.of(published, ownedArchived, archived)
        );

        assertThat(enrollmentByCourseId).containsEntry("archived", true);
        assertThat(enrollmentByCourseId).doesNotContainKeys("published", "owned");
    }

    @Test
    void ensureCurrentUserOwnsRejectsNonOwner() {
        Course course = course(CourseStatus.PUBLISHED, "inst-1");

        assertThatThrownBy(() -> courseAccessService.ensureCurrentUserOwns("inst-2", course))
                .isInstanceOf(ForbiddenException.class);
    }

    private void authenticateAdmin(String userId) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
    }

    private Course course(CourseStatus status, String instructorId) {
        return course("course-1", status, instructorId);
    }

    private Course course(String courseId, CourseStatus status, String instructorId) {
        Course course = new Course();
        course.setId(courseId);
        course.setStatus(status);
        course.setInstructorId(instructorId);
        return course;
    }
}
