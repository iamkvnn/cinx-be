package com.cinx.course.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.response.CheckEnrollmentStatus;
import com.cinx.course.model.Course;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.service.enrollment.EnrollmentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAccessService implements ICourseAccessService {
    private final CourseRepository courseRepository;
    private final EnrollmentClient enrollmentClient;

    @Override
    @Transactional(readOnly = true)
    public Course ensureReadableCourse(String currentUserId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!canReadCourse(currentUserId, course)) {
            throw new NotFoundException("Course not found with id: " + courseId);
        }
        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public Course ensureManageableCourse(String currentUserId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id: " + courseId));
        if (!canManageCourse(currentUserId, course)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "You are not allowed to access this course");
        }
        return course;
    }

    @Override
    public boolean canReadCourse(String currentUserId, Course course) {
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return true;
        }
        if (course.getStatus() != CourseStatus.ARCHIVED || currentUserId == null) {
            return false;
        }
        return isAdmin()
                || isCourseOwner(currentUserId, course)
                || isEnrolled(currentUserId, course.getId());
    }

    @Override
    public boolean canReadCourse(String currentUserId, Course course, Map<String, Boolean> enrollmentByCourseId) {
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return true;
        }
        if (course.getStatus() != CourseStatus.ARCHIVED || currentUserId == null) {
            return false;
        }
        return isAdmin()
                || isCourseOwner(currentUserId, course)
                || Boolean.TRUE.equals(enrollmentByCourseId.get(course.getId()));
    }

    @Override
    public boolean canManageCourse(String currentUserId, Course course) {
        return isAdmin() || isCourseOwner(currentUserId, course);
    }

    @Override
    public Map<String, Boolean> enrollmentByCourseId(String currentUserId, List<Course> courses) {
        if (currentUserId == null || isAdmin()) {
            return Map.of();
        }
        List<String> courseIds = courses.stream()
                .filter(course -> course.getStatus() == CourseStatus.ARCHIVED)
                .filter(course -> !isCourseOwner(currentUserId, course))
                .map(Course::getId)
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        ApiResponse<List<CheckEnrollmentStatus>> response = enrollmentClient.checkEnrollmentStatus(currentUserId, courseIds);
        if (response == null || response.data() == null) {
            return Map.of();
        }
        return response.data().stream()
                .collect(Collectors.toMap(
                        CheckEnrollmentStatus::courseId,
                        status -> Boolean.TRUE.equals(status.isEnrolled())
                ));
    }

    @Override
    public boolean isAdmin() {
        Authentication authentication = AuthenticationUtil.getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    @Override
    public boolean isCourseOwner(String currentUserId, Course course) {
        return currentUserId != null && Objects.equals(course.getInstructorId(), currentUserId);
    }

    @Override
    public boolean isEnrolled(String currentUserId, String courseId) {
        if (currentUserId == null) {
            return false;
        }
        ApiResponse<List<CheckEnrollmentStatus>> response = enrollmentClient.checkEnrollmentStatus(currentUserId, List.of(courseId));
        return response != null
                && response.data() != null
                && response.data().stream()
                        .anyMatch(status -> courseId.equals(status.courseId()) && Boolean.TRUE.equals(status.isEnrolled()));
    }

    @Override
    public void ensureCurrentUserOwns(String currentUserId, Course course) {
        if (isAdmin()) {
            return;
        }
        if (!isCourseOwner(currentUserId, course)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "You are not allowed to access this course");
        }
    }
}
