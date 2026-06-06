package com.cinx.learning.service.authorization;

import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.dto.response.CourseResponse;
import com.cinx.learning.model.AssignmentSubmission;
import com.cinx.learning.model.QuizSession;
import com.cinx.learning.repository.AssignmentSubmissionRepository;
import com.cinx.learning.repository.QuizSessionRepository;
import com.cinx.learning.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LearningAuthorizationService {
    private final CourseService courseService;
    private final QuizSessionRepository quizSessionRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;

    public String currentUserId() {
        return AuthenticationUtil.extractUserId();
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isInstructor() {
        return hasRole("INSTRUCTOR");
    }

    public void requireCourseInstructorOrAdmin(String courseId) {
        if (isAdmin() || isCourseInstructor(courseId, currentUserId())) {
            return;
        }
        throw new ForbiddenException("Only the course instructor or admin can access this resource");
    }

    public void requireLessonInstructorOrAdmin(String lessonId) {
        if (isAdmin() || isLessonInstructor(lessonId, currentUserId())) {
            return;
        }
        throw new ForbiddenException("Only the lesson instructor or admin can access this resource");
    }

    public QuizSession requireQuizSessionOwner(String sessionId) {
        QuizSession session = findQuizSession(sessionId);
        if (Objects.equals(session.getUserId(), currentUserId())) {
            return session;
        }
        throw new ForbiddenException("Only the quiz session owner can access this resource");
    }

    public QuizSession requireQuizSessionOwnerOrInstructorOrAdmin(String sessionId) {
        QuizSession session = findQuizSession(sessionId);
        if (Objects.equals(session.getUserId(), currentUserId()) || isAdmin() || isLessonInstructor(session.getQuizLessonId(), currentUserId())) {
            return session;
        }
        throw new ForbiddenException("Only the quiz session owner, lesson instructor, or admin can access this resource");
    }

    public QuizSession requireQuizSessionInstructorOrAdmin(String sessionId) {
        QuizSession session = findQuizSession(sessionId);
        if (isAdmin() || isLessonInstructor(session.getQuizLessonId(), currentUserId())) {
            return session;
        }
        throw new ForbiddenException("Only the lesson instructor or admin can access this resource");
    }

    public AssignmentSubmission requireAssignmentSubmissionInstructorOrAdmin(String submissionId) {
        AssignmentSubmission submission = findAssignmentSubmission(submissionId);
        if (isAdmin() || isLessonInstructor(submission.getAssignmentId(), currentUserId())) {
            return submission;
        }
        throw new ForbiddenException("Only the assignment instructor or admin can access this resource");
    }

    public AssignmentSubmission requireAssignmentSubmissionDeleteAllowed(String submissionId) {
        AssignmentSubmission submission = findAssignmentSubmission(submissionId);
        if (isAdmin() || isLessonInstructor(submission.getAssignmentId(), currentUserId())) {
            return submission;
        }
        if (Objects.equals(submission.getUserId(), currentUserId()) && submission.getScore() == null) {
            return submission;
        }
        throw new ForbiddenException("Only the submission owner before grading, assignment instructor, or admin can delete this submission");
    }

    public boolean isCourseInstructor(String courseId, String userId) {
        if (userId == null) {
            return false;
        }
        CourseResponse course = courseService.getCourseById(courseId).data();
        return course != null && course.instructor() != null && Objects.equals(course.instructor().id(), userId);
    }

    public boolean isLessonInstructor(String lessonId, String userId) {
        if (userId == null) {
            return false;
        }
        var response = courseService.isLessonInstructor(lessonId, userId);
        return response != null && Boolean.TRUE.equals(response.data());
    }

    private boolean hasRole(String role) {
        Authentication authentication = AuthenticationUtil.getAuthentication();
        if (authentication == null) {
            return false;
        }
        String authority = "ROLE_" + role;
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private QuizSession findQuizSession(String sessionId) {
        return quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));
    }

    private AssignmentSubmission findAssignmentSubmission(String submissionId) {
        return assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }
}
