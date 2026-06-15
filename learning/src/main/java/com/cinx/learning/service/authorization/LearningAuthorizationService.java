package com.cinx.learning.service.authorization;

import com.cinx.common.exception.ErrorCode;
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

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isInstructor() {
        return hasRole("INSTRUCTOR");
    }

    public void requireCourseInstructor(String currentUserId, String courseId) {
        if (isCourseInstructor(courseId, currentUserId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "Only the course instructor can access this resource");
    }

    public void requireLessonInstructor(String currentUserId, String lessonId) {
        if (isLessonInstructor(lessonId, currentUserId)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "Only the lesson instructor can access this resource");
    }

    public QuizSession requireQuizSessionOwner(String currentUserId, String sessionId) {
        QuizSession session = findQuizSession(sessionId);
        if (Objects.equals(session.getUserId(), currentUserId)) {
            return session;
        }
        throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Only the quiz session owner can access this resource");
    }

    public QuizSession requireQuizSessionOwnerOrInstructor(String currentUserId, String sessionId) {
        QuizSession session = findQuizSession(sessionId);
        if (Objects.equals(session.getUserId(), currentUserId) || isLessonInstructor(session.getQuizLessonId(), currentUserId)) {
            return session;
        }
        throw new ForbiddenException(ErrorCode.FORBIDDEN, "Only the quiz session owner or lesson instructor can access this resource");
    }

    public QuizSession requireQuizSessionInstructor(String currentUserId, String sessionId) {
        QuizSession session = findQuizSession(sessionId);
        if (isLessonInstructor(session.getQuizLessonId(), currentUserId)) {
            return session;
        }
        throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "Only the lesson instructor can access this resource");
    }

    public AssignmentSubmission requireAssignmentSubmissionInstructor(String currentUserId, String submissionId) {
        AssignmentSubmission submission = findAssignmentSubmission(submissionId);
        if (isLessonInstructor(submission.getAssignmentId(), currentUserId)) {
            return submission;
        }
        throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "Only the assignment instructor can access this resource");
    }

    public AssignmentSubmission requireAssignmentSubmissionDeleteAllowed(String currentUserId, String submissionId) {
        AssignmentSubmission submission = findAssignmentSubmission(submissionId);
        if (isLessonInstructor(submission.getAssignmentId(), currentUserId)) {
            return submission;
        }
        if (Objects.equals(submission.getUserId(), currentUserId) && submission.getScore() == null) {
            return submission;
        }
        throw new ForbiddenException(ErrorCode.FORBIDDEN, "Only the submission owner before grading or assignment instructor can delete this submission");
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
