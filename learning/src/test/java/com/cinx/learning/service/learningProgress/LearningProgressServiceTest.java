package com.cinx.learning.service.learningProgress;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.learning.consts.LessonType;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningProgressServiceTest {
    @Mock
    private CourseProgressRepository courseProgressRepository;
    @Mock
    private LearningItemProgressRepository learningItemProgressRepository;
    @Mock
    private CourseProgressMapper courseProgressMapper;
    @Mock
    private LearningItemProgressMapper learningItemProgressMapper;
    @Mock
    private CourseService courseService;
    @Mock
    private ILearningPathService learningPathService;
    @Mock
    private IStreakService streakService;
    @Mock
    private IDailyGoalService dailyGoalService;
    @Mock
    private EnrollmentClient enrollmentClient;
    @Mock
    private NotificationPublisher notificationPublisher;
    @Spy
    private CourseProgressCalculator courseProgressCalculator = new CourseProgressCalculator();

    @InjectMocks
    private LearningProgressService learningProgressService;

    @Test
    void completeArticleItemRejectsNonArticleLesson() {
        CourseProgress courseProgress = courseProgress("course-1", 1);
        LearningItemProgress progress = item("video-1", courseProgress, false, false, null);
        LessonResponse videoLesson = new LessonResponse("video-1", "Video", 120L, LessonType.VIDEO, 1);

        when(learningItemProgressRepository.findByItemIdAndUserIdForUpdate("video-1", "user-1"))
                .thenReturn(Optional.of(progress));
        when(courseService.getEnrolledLessonById("course-1", "video-1"))
                .thenReturn(new ApiResponse<>(true, "ok", videoLesson));

        assertThatThrownBy(() -> learningProgressService.completeArticleItem("user-1", "video-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only article lessons");
    }

    @Test
    void failedItemCompletesButDoesNotPassCourse() {
        CourseProgress courseProgress = courseProgress("course-1", 1);
        LearningItemProgress failedQuiz = item("quiz-1", courseProgress, false, false, null);

        when(learningItemProgressRepository.findByItemIdAndUserIdForUpdate("quiz-1", "user-1"))
                .thenReturn(Optional.of(failedQuiz));
        when(learningItemProgressRepository.findAllByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(List.of(failedQuiz));

        LearningItemProgressUpdateResult result = learningProgressService.updateLearningItemProgress(
                "user-1",
                "quiz-1",
                new UpdateLearningItemRequest(true, false, 4.0));

        assertThat(result.completedTransition()).isTrue();
        assertThat(result.passedTransition()).isFalse();
        assertThat(result.courseCompletedTransition()).isTrue();
        assertThat(result.coursePassedTransition()).isFalse();
        assertThat(failedQuiz.getIsCompleted()).isTrue();
        assertThat(failedQuiz.getIsPassed()).isFalse();
        assertThat(failedQuiz.getScore()).isEqualTo(4.0);
        assertThat(courseProgress.getCompletedItems()).isEqualTo(1);
        assertThat(courseProgress.getAvgScore()).isEqualTo(4.0);
        assertThat(courseProgress.getIsCompleted()).isTrue();
        assertThat(courseProgress.getIsPassed()).isFalse();
    }

    @Test
    void failedItemLaterPassedDoesNotRepeatCompletionSideEffects() {
        CourseProgress courseProgress = courseProgress("course-1", 1);
        LearningItemProgress failedQuiz = item("quiz-1", courseProgress, true, false, 4.0);
        courseProgress.setCompletedItems(1);
        courseProgress.setIsCompleted(true);

        when(learningItemProgressRepository.findByItemIdAndUserIdForUpdate("quiz-1", "user-1"))
                .thenReturn(Optional.of(failedQuiz));
        when(learningItemProgressRepository.findAllByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(List.of(failedQuiz));

        LearningItemProgressUpdateResult result = learningProgressService.updateLearningItemProgress(
                "user-1",
                "quiz-1",
                new UpdateLearningItemRequest(true, true, 7.0));

        assertThat(result.completedTransition()).isFalse();
        assertThat(result.passedTransition()).isTrue();
        assertThat(result.courseCompletedTransition()).isFalse();
        assertThat(result.coursePassedTransition()).isTrue();
        assertThat(failedQuiz.getIsCompleted()).isTrue();
        assertThat(failedQuiz.getIsPassed()).isTrue();
        assertThat(courseProgress.getCompletedItems()).isEqualTo(1);
        assertThat(courseProgress.getAvgScore()).isEqualTo(7.0);
        assertThat(courseProgress.getIsCompleted()).isTrue();
        assertThat(courseProgress.getIsPassed()).isTrue();
    }

    @Test
    void passingFinalItemCompletesAndPassesCourse() {
        CourseProgress courseProgress = courseProgress("course-1", 2);
        LearningItemProgress quiz = item("quiz-1", courseProgress, false, false, null);
        LearningItemProgress article = item("article-1", courseProgress, true, true, 8.0);

        when(learningItemProgressRepository.findByItemIdAndUserIdForUpdate("quiz-1", "user-1"))
                .thenReturn(Optional.of(quiz));
        when(learningItemProgressRepository.findAllByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(List.of(quiz, article));

        LearningItemProgressUpdateResult result = learningProgressService.updateLearningItemProgress(
                "user-1",
                "quiz-1",
                new UpdateLearningItemRequest(true, true, 10.0));

        assertThat(result.completedTransition()).isTrue();
        assertThat(result.passedTransition()).isTrue();
        assertThat(result.courseCompletedTransition()).isTrue();
        assertThat(result.coursePassedTransition()).isTrue();
        assertThat(quiz.getIsCompleted()).isTrue();
        assertThat(quiz.getIsPassed()).isTrue();
        assertThat(courseProgress.getCompletedItems()).isEqualTo(2);
        assertThat(courseProgress.getAvgScore()).isEqualTo(9.0);
        assertThat(courseProgress.getIsCompleted()).isTrue();
        assertThat(courseProgress.getIsPassed()).isTrue();
        assertThat(courseProgress.getCompletionTime()).isNotNull();
        verify(notificationPublisher).publishCourseCompleted("user-1", "course-1", "your course");
    }

    private CourseProgress courseProgress(String courseId, int totalItems) {
        CourseProgress courseProgress = new CourseProgress();
        courseProgress.setCourseId(courseId);
        courseProgress.setTotalItems(totalItems);
        courseProgress.setCompletedItems(0);
        courseProgress.setIsCompleted(false);
        courseProgress.setIsPassed(false);
        return courseProgress;
    }

    private LearningItemProgress item(String itemId, CourseProgress courseProgress, boolean completed, boolean passed, Double score) {
        LearningItemProgress progress = new LearningItemProgress();
        progress.setItemId(itemId);
        progress.setCourseProgress(courseProgress);
        progress.setIsCompleted(completed);
        progress.setIsPassed(passed);
        progress.setScore(score);
        return progress;
    }
}
