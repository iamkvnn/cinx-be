package com.cinx.learning.service.quiz;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.GradeEssayRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.QuizLessonResponse;
import com.cinx.learning.dto.response.QuizSessionQuestionResponse;
import com.cinx.learning.dto.response.QuizSessionResponse;
import com.cinx.learning.mapper.QuizSessionMapper;
import com.cinx.learning.model.QuizSession;
import com.cinx.learning.model.QuizSessionQuestion;
import com.cinx.learning.model.QuizSessionSubmission;
import com.cinx.learning.repository.QuizSessionQuestionRepository;
import com.cinx.learning.repository.QuizSessionRepository;
import com.cinx.learning.repository.QuizSessionSubmissionRepository;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.learningProgress.LearningItemProgressUpdateResult;
import com.cinx.learning.service.quiz.evaluator.QuestionEvaluatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {
    @Mock
    private QuizScoreAggregator quizScoreAggregator;
    @Mock
    private QuestionEvaluatorFactory questionEvaluatorFactory;
    @Mock
    private QuizSessionRepository quizSessionRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private QuizSessionMapper quizSessionMapper;
    @Mock
    private QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    @Mock
    private QuizSessionQuestionRepository quizSessionQuestionRepository;
    @Mock
    private ILearningProgressService learningProgressService;
    @Mock
    private IDailyGoalService dailyGoalService;
    @Mock
    private QuizSnapshotBuilder snapshotBuilder;

    @InjectMocks
    private QuizService quizService;

    @Test
    void createQuizSessionRejectsExistingInProgressSession() {
        when(courseService.getQuizLessonById("course-1", "quiz-1"))
                .thenReturn(new ApiResponse<>(true, "ok", quizLesson()));
        when(quizSessionRepository.existsByQuizLessonIdAndUserIdAndStatus(
                "quiz-1",
                "user-1",
                QuizSessionStatus.IN_PROGRESS))
                .thenReturn(true);

        assertThatThrownBy(() -> quizService.createQuizSession("course-1", "user-1", "quiz-1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("in-progress");
    }

    @Test
    void submitQuizSessionRejectsDuplicateAnswers() {
        QuizSession session = session(QuizSessionStatus.IN_PROGRESS);
        when(quizSessionRepository.findById("session-1")).thenReturn(Optional.of(session));

        SubmitQuizSessionRequest request = new SubmitQuizSessionRequest(List.of(
                new ChooseQuizAnswerRequest("q-1", "a"),
                new ChooseQuizAnswerRequest("q-1", "b")));

        assertThatThrownBy(() -> quizService.submitQuizSession("session-1", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Duplicate answer");
    }

    @Test
    void gradeEssayMovesSessionToGradedAndRecordsQuizPassedTransition() {
        QuizSession session = session(QuizSessionStatus.PENDING_GRADE);
        QuizSessionQuestion essay = QuizSessionQuestion.builder()
                .quizSessionId("session-1")
                .questionId("essay-1")
                .questionType(QuizQuestionType.ESSAY)
                .score(0.0)
                .build();
        QuizSessionSubmission submission = QuizSessionSubmission.builder()
                .quizSessionId("session-1")
                .score(0.0)
                .build();
        QuizSessionResponse response = new QuizSessionResponse(
                "session-1",
                "quiz-1",
                session.getStartTime(),
                session.getEndTime(),
                QuizSessionStatus.GRADED,
                true,
                true,
                null);

        when(quizSessionRepository.findById("session-1")).thenReturn(Optional.of(session));
        when(quizSessionQuestionRepository.findAllEssayByQuizSessionId("session-1")).thenReturn(List.of(essay));
        when(quizSessionQuestionRepository.findAllByQuizSessionId(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(essay)));
        when(quizSessionSubmissionRepository.findByQuizSessionId("session-1")).thenReturn(Optional.of(submission));
        when(quizSessionSubmissionRepository.save(submission)).thenReturn(submission);
        when(quizScoreAggregator.aggregateScore("course-1", "user-1", "quiz-1")).thenReturn(8.0);
        when(learningProgressService.updateLearningItemProgress(
                "user-1",
                "quiz-1",
                new UpdateLearningItemRequest(true, true, 8.0)))
                .thenReturn(new LearningItemProgressUpdateResult(false, true, false, false));
        when(quizSessionMapper.toDto(session)).thenReturn(response);

        QuizSessionResponse result = quizService.gradeEssay(
                "session-1",
                new GradeEssayRequest(List.of(new GradeEssayRequest.EssayQuestionScore("essay-1", 8.0))));

        assertThat(session.getStatus()).isEqualTo(QuizSessionStatus.GRADED);
        assertThat(result.status()).isEqualTo(QuizSessionStatus.GRADED);
        verify(dailyGoalService).recordProgress("user-1", DailyGoalType.QUIZZES_PASSED, 1);
    }

    @Test
    void gradedSessionReviewHonorsReviewAllowedAndShowsAnswersWhenAllowed() {
        QuizSession blocked = session(QuizSessionStatus.GRADED);
        blocked.setIsReviewAllowed(false);
        when(quizSessionRepository.findById("blocked")).thenReturn(Optional.of(blocked));

        assertThatThrownBy(() -> quizService.getQuizSessionQuestions("blocked", 1, 10))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Review is not allowed");

        QuizSession allowed = session(QuizSessionStatus.GRADED);
        QuizSessionQuestion question = QuizSessionQuestion.builder()
                .id("session-question-1")
                .quizSessionId("allowed")
                .questionId("q-1")
                .questionType(QuizQuestionType.SINGLE_CHOICE)
                .questionOrder(1)
                .questionText("Question?")
                .userAnswer("a")
                .correctAnswer("b")
                .score(0.0)
                .build();
        when(quizSessionRepository.findById("allowed")).thenReturn(Optional.of(allowed));
        when(quizSessionQuestionRepository.findAllByQuizSessionId("allowed", org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(question)));
        when(snapshotBuilder.parseOptionsSnapshot(null)).thenReturn(List.of());

        Page<QuizSessionQuestionResponse> response = quizService.getQuizSessionQuestions("allowed", 1, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().correctAnswer()).isEqualTo("b");
        assertThat(response.getContent().getFirst().score()).isEqualTo(0.0);
    }

    private QuizSession session(QuizSessionStatus status) {
        return QuizSession.builder()
                .id("session-1")
                .courseId("course-1")
                .userId("user-1")
                .quizLessonId("quiz-1")
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now().plusMinutes(10))
                .status(status)
                .isReviewAllowed(true)
                .isShowAnswersOnReview(true)
                .build();
    }

    private QuizLessonResponse quizLesson() {
        return new QuizLessonResponse(
                1,
                3,
                30,
                true,
                true,
                false,
                false,
                null,
                List.of());
    }
}
