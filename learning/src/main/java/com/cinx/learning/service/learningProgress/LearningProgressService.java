package com.cinx.learning.service.learningProgress;

import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.CourseDetailResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.dto.response.LessonResponse;
import com.cinx.learning.mapper.CourseProgressMapper;
import com.cinx.learning.mapper.LearningItemProgressMapper;
import com.cinx.learning.messaging.NotificationPublisher;
import com.cinx.learning.model.CourseProgress;
import com.cinx.learning.model.LearningItemProgress;
import com.cinx.learning.repository.CourseProgressRepository;
import com.cinx.learning.repository.LearningItemProgressRepository;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import com.cinx.learning.service.enrollment.EnrollmentClient;
import com.cinx.learning.service.learningPath.ILearningPathService;
import com.cinx.learning.service.streak.IStreakService;
import com.cinx.learning.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningProgressService implements ILearningProgressService {
    private final CourseProgressRepository courseProgressRepository;
    private final LearningItemProgressRepository learningItemProgressRepository;
    private final CourseProgressMapper courseProgressMapper;
    private final LearningItemProgressMapper learningItemProgressMapper;
    private final CourseService courseService;
    private final ILearningPathService learningPathService;
    private final IStreakService streakService;
    private final IDailyGoalService dailyGoalService;
    private final EnrollmentClient enrollmentClient;
    private final UserService userService;
    private final NotificationPublisher notificationPublisher;

    @Override
    public List<CourseProgressResponse> getCourseProgressByCourseIds(String userId, List<String> courseIds) {
        return courseProgressRepository.findAllByUserIdAndCourseIdIn(userId, courseIds)
                .stream()
                .map(courseProgressMapper::toDto)
                .toList();
    }

    @Override
    public CourseProgressResponse getCourseProgress(String userId, String courseId) {
        return courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .map(courseProgressMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Course progress not found"));
    }

    @Override
    public List<LearningItemProgressResponse> getLearningItemProgressByCourseId(String userId, String courseId) {
        return learningItemProgressRepository.findAllByUserIdAndCourseId(userId, courseId)
                .stream()
                .map(learningItemProgressMapper::toDto)
                .toList();
    }

    @Override
    public List<CourseProgressResponse> getCourseProgressByCourseId(String courseId) {
        return courseProgressRepository.findAllByCourseId(courseId)
                .stream()
                .map(courseProgressMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void createCourseProgress(String userId, String courseId) {
        List<String> lessonIds = courseService.getCourseLessonIdsByCourseId(courseId).data();
        CourseProgress courseProgress = courseProgressRepository.save(
                CourseProgress.builder()
                        .isCompleted(false)
                        .userId(userId)
                        .courseId(courseId)
                        .totalItems(lessonIds.size())
                        .completedItems(0)
                        .build()
        );
        learningItemProgressRepository.saveAll(lessonIds.stream()
                .map(lessonId -> LearningItemProgress.builder()
                        .isCompleted(false)
                        .courseProgress(courseProgress)
                        .itemId(lessonId)
                        .build())
                .toList());
    }

    @Transactional
    @Override
    public void updateLearningItemProgress(String userId, String itemId, UpdateLearningItemRequest request) {
        LearningItemProgress progress = learningItemProgressRepository
                .findByItemIdAndUserId(itemId, userId)
                .orElseThrow(() -> new NotFoundException("Learning item progress not found"));

        CourseProgress course = progress.getCourseProgress();
        Boolean oldCompleted = progress.getIsCompleted();
        Double oldScore = progress.getScore();
        Boolean newCompleted = request.isCompleted() != null ? request.isCompleted() : oldCompleted;
        Double newScore = request.score();

        int completedItems = course.getCompletedItems() != null ? course.getCompletedItems() : 0;
        double totalScore = (course.getAvgScore() != null ? course.getAvgScore() : 0.0) * completedItems;

        if (!Boolean.TRUE.equals(oldCompleted) && Boolean.TRUE.equals(newCompleted)) {
            completedItems++;
            totalScore += newScore;
            progress.setIsCompleted(true);
            progress.setScore(newScore);
            progress.setIsPassed(request.isPassed());
            streakService.updateStreakOnActivity(userId);
            dailyGoalService.addXp(userId, 50);
            learningPathService.updatePathProgress(userId, itemId);
        } else if (Boolean.TRUE.equals(oldCompleted) && Boolean.TRUE.equals(newCompleted)) {
            totalScore = totalScore - (oldScore != null ? oldScore : 0.0) + newScore;
            progress.setScore(newScore);
            progress.setIsPassed(request.isPassed());
        }

        course.setCompletedItems(completedItems);
        course.setAvgScore(completedItems > 0 ? totalScore / completedItems : 0.0);

        boolean justCompleted = !Boolean.TRUE.equals(oldCompleted) && course.getTotalItems() != null && completedItems == course.getTotalItems();

        if (justCompleted) {
            course.setIsCompleted(true);
            course.setCompletionTime(LocalDateTime.now());
            
            // Notify user of course completion
            try {
                String courseTitle = "your course";
                var courseRes = courseService.getCourseById(course.getCourseId());
                if (courseRes != null && courseRes.success() && courseRes.data() != null) {
                    courseTitle = courseRes.data().title();
                }

                String title = "Course Completed!";
                String message = "Congratulations on completing: " + courseTitle;

                notificationPublisher.sendInApp(List.of(userId), title, message);
                notificationPublisher.sendPush(List.of(userId), title, message, Map.of("courseId", course.getCourseId()));
            } catch (Exception ex) {
                log.error("Failed to send course completion notification for userId={}, courseId={}", userId, course.getCourseId(), ex);
            }
        } else if (course.getTotalItems() != null && completedItems == course.getTotalItems()) {
            course.setIsCompleted(true);
            course.setCompletionTime(LocalDateTime.now());
        }

        learningItemProgressRepository.save(progress);
        courseProgressRepository.save(course);
    }

    @Transactional
    @Override
    public void recomputeCourseProgress(String courseId, String lessonId, String changeType) {
        log.info("Recomputing course progress for courseId={} lessonId={} changeType={}",
                courseId, lessonId, changeType);

        // 1. Fetch the current, authoritative lesson set from the course service.
        List<String> currentLessonIds;
        try {
            currentLessonIds = courseService.getCourseLessonIdsByCourseId(courseId).data();
        } catch (Exception ex) {
            log.error("Cannot fetch course detail for courseId={}, aborting recompute", courseId, ex);
            return;
        }

        int expectedTotal = currentLessonIds.size();

        // 2. Get all enrolled users for this course.
        List<String> enrolledUserIds;
        try {
            enrolledUserIds = enrollmentClient.getUserIdsEnrolledInCourse(courseId).data();
        } catch (Exception ex) {
            log.error("Cannot fetch enrolled users for courseId={}, aborting recompute", courseId, ex);
            return;
        }

        if (enrolledUserIds == null || enrolledUserIds.isEmpty()) {
            log.info("No enrolled users for courseId={}, nothing to recompute", courseId);
            return;
        }

        // 3. Recompute each user's progress.
        for (String userId : enrolledUserIds) {
            try {
                recomputeForUser(userId, courseId, currentLessonIds, expectedTotal);
                learningPathService.refreshPrerequisiteUnlocks(userId, courseId);
            } catch (Exception ex) {
                log.error("Failed to recompute progress for userId={} courseId={}", userId, courseId, ex);
                // Continue with next user — don't fail the entire batch.
            }
        }
    }

    private void recomputeForUser(String userId, String courseId,
                                  List<String> currentLessonIds, int expectedTotal) {
        CourseProgress courseProgress = courseProgressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        if (courseProgress == null) {
            log.debug("No CourseProgress for userId={} courseId={}, skipping", userId, courseId);
            return;
        }

        // Existing item-progress rows for this user/course.
        List<LearningItemProgress> existingItems =
                learningItemProgressRepository.findAllByUserIdAndCourseId(userId, courseId);

        Map<String, LearningItemProgress> existingByItemId = existingItems.stream()
                .collect(Collectors.toMap(LearningItemProgress::getItemId, i -> i));

        Set<String> currentSet = new HashSet<>(currentLessonIds);

        // Remove stale rows (deleted lessons).
        List<LearningItemProgress> toDelete = existingItems.stream()
                .filter(item -> !currentSet.contains(item.getItemId()))
                .toList();
        if (!toDelete.isEmpty()) {
            learningItemProgressRepository.deleteAll(toDelete);
            log.debug("Removed {} stale item-progress row(s) for userId={} courseId={}",
                    toDelete.size(), userId, courseId);
        }

        // Create missing rows (new lessons).
        List<LearningItemProgress> toAdd = currentLessonIds.stream()
                .filter(lid -> !existingByItemId.containsKey(lid))
                .<LearningItemProgress>map(lid -> LearningItemProgress.builder()
                        .itemId(lid)
                        .isCompleted(false)
                        .courseProgress(courseProgress)
                        .build())
                .toList();
        if (!toAdd.isEmpty()) {
            learningItemProgressRepository.saveAll(toAdd);
            log.debug("Added {} new item-progress row(s) for userId={} courseId={}",
                    toAdd.size(), userId, courseId);
        }

        // Recompute aggregates from the remaining completed rows.
        List<LearningItemProgress> remainingCompleted = existingItems.stream()
                .filter(item -> currentSet.contains(item.getItemId()))
                .filter(item -> Boolean.TRUE.equals(item.getIsCompleted()))
                .toList();

        int completedCount = remainingCompleted.size();
        double avgScore = 0.0;
        if (completedCount > 0) {
            double total = remainingCompleted.stream()
                    .mapToDouble(item -> item.getScore() != null ? item.getScore() : 0.0)
                    .sum();
            avgScore = total / completedCount;
        }

        courseProgress.setTotalItems(expectedTotal);
        courseProgress.setCompletedItems(completedCount);
        courseProgress.setAvgScore(avgScore);

        boolean nowComplete = expectedTotal > 0 && completedCount == expectedTotal;
        boolean justCompleted = nowComplete && !Boolean.TRUE.equals(courseProgress.getIsCompleted());

        courseProgress.setIsCompleted(nowComplete);
        if (nowComplete && courseProgress.getCompletionTime() == null) {
            courseProgress.setCompletionTime(LocalDateTime.now());
        } else if (!nowComplete) {
            courseProgress.setCompletionTime(null);
        }

        courseProgressRepository.save(courseProgress);

        if (justCompleted) {
            try {
                String courseTitle = "your course";
                var courseRes = courseService.getCourseById(courseId);
                if (courseRes != null && courseRes.success() && courseRes.data() != null) {
                    courseTitle = courseRes.data().title();
                }

                String title = "Course Completed!";
                String message = "Congratulations on completing: " + courseTitle;

                notificationPublisher.sendInApp(List.of(userId), title, message);
                notificationPublisher.sendPush(List.of(userId), title, message, Map.of("courseId", courseId));
            } catch (Exception ex) {
                log.error("Failed to send course completion notification for userId={}, courseId={}", userId, courseId, ex);
            }
        }
    }

    private void publishLessonChangeNotifications(String courseId, String changeType,
                                                  List<String> enrolledUserIds,
                                                  String courseTitle) {
        String subject = "Update in your course: " + courseTitle;
        String body = buildEmailBody(courseTitle, changeType);
        String inAppMessage = buildInAppMessage(courseTitle, changeType);

        // In-app: single batch message for all users.
        notificationPublisher.sendInApp(enrolledUserIds, subject, inAppMessage);

        // Email: one message per user (requires their email address).
        for (String userId : enrolledUserIds) {
            try {
                String email = userService.getUserById(userId).data().email();
                if (email != null && !email.isBlank()) {
                    notificationPublisher.sendEmail(email, subject, body);
                }
            } catch (Exception ex) {
                log.warn("Could not send email notification to userId={}: {}", userId, ex.getMessage());
            }
        }
    }

    private String buildEmailBody(String courseTitle, String changeType) {
        String action = switch (changeType) {
            case "CREATED"              -> "A new lesson has been added";
            case "DELETED"              -> "A lesson has been removed";
            case "PREREQUISITE_CHANGED" -> "Lesson prerequisites have been updated";
            case "ORDER_CHANGED"        -> "Lesson order has been updated";
            default                     -> "Lesson content has been updated";
        };
        return "<html><body>"
                + "<h2>Course Update: " + courseTitle + "</h2>"
                + "<p>" + action + " in the course <strong>" + courseTitle + "</strong>.</p>"
                + "<p>Your learning progress has been automatically recalculated. "
                + "Log in to see the latest state of your course.</p>"
                + "</body></html>";
    }

    private String buildInAppMessage(String courseTitle, String changeType) {
        return switch (changeType) {
            case "CREATED"              -> "A new lesson was added to \"" + courseTitle + "\".";
            case "DELETED"              -> "A lesson was removed from \"" + courseTitle + "\". Your progress has been updated.";
            case "PREREQUISITE_CHANGED" -> "Lesson prerequisites changed in \"" + courseTitle + "\". Check your learning path.";
            case "ORDER_CHANGED"        -> "Lesson order has changed in \"" + courseTitle + "\".";
            default                     -> "Lesson content updated in \"" + courseTitle + "\".";
        };
    }
}
