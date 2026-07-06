package com.cinx.learning.service.quiz;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.GradeEssayRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.*;
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
import com.cinx.learning.service.quiz.evaluator.IQuestionEvaluator;
import com.cinx.learning.service.quiz.evaluator.QuestionEvaluatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {
    private static final double PASSING_SCORE = 5.0;

    private final QuizScoreAggregator quizScoreAggregator;
    private final QuestionEvaluatorFactory questionEvaluatorFactory;
    private final QuizSessionRepository quizSessionRepository;
    private final CourseService courseService;
    private final QuizSessionMapper quizSessionMapper;
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final QuizSessionQuestionRepository quizSessionQuestionRepository;
    private final ILearningProgressService learningProgressService;
    private final IDailyGoalService dailyGoalService;
    private final QuizSnapshotBuilder snapshotBuilder;

    @Override
    public Page<QuizSessionResponse> getQuizSessions(String userId, String lessonId, int page, int size, String sort) {
        validatePageRequest(page, size);
        return quizSessionRepository.findAllByQuizLessonId(lessonId, userId, PageRequest.of(page - 1, size, SortConverter.toSort(sort)))
                .map(quizSessionMapper::toDto);
    }

    @Override
    public QuizSessionResponse getQuizSession(String id) {
        return quizSessionRepository.findById(id)
                .map(quizSessionMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));
    }

    @Override
    public Page<QuizSessionQuestionResponse> getQuizSessionQuestions(String quizSessionId, int page, int size, String sort) {
        validatePageRequest(page, size);
        QuizSession quizSession = quizSessionRepository.findById(quizSessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));

        if (isReviewStatus(quizSession.getStatus())) {
            if (Boolean.FALSE.equals(quizSession.getIsReviewAllowed())) {
                throw new BadRequestException(ErrorCode.QUIZ_REVIEW_NOT_ALLOWED, "Review is not allowed for this quiz session");
            }
        }

        boolean inProgress = quizSession.getStatus() == QuizSessionStatus.IN_PROGRESS;
        boolean hideAnswers = Boolean.FALSE.equals(quizSession.getIsShowAnswersOnReview());

        return quizSessionQuestionRepository
                .findAllByQuizSessionId(quizSessionId, PageRequest.of(page - 1, size, SortConverter.toSort(sort)))
                .map(q -> buildResponse(q, inProgress || hideAnswers));
    }

    private QuizSessionQuestionResponse buildResponse(QuizSessionQuestion q, boolean hideAnswers) {
        List<QuizSessionOptionResponse> options = snapshotBuilder.parseOptionsSnapshot(q.getOptionsSnapshot());
        return new QuizSessionQuestionResponse(
                q.getId(),
                q.getQuizSessionId(),
                q.getQuestionId(),
                q.getQuestionType(),
                q.getScoringMethod(),
                q.getQuestionOrder(),
                q.getQuestionText(),
                q.getUserAnswer(),
                hideAnswers ? null : q.getCorrectAnswer(),
                hideAnswers ? null : q.getScore(),
                options);
    }

    @Transactional
    @Override
    public QuizSessionResponse createQuizSession(String courseId, String userId, String lessonId) {
        QuizLessonResponse quizLessonResponse = courseService.getQuizLessonById(courseId, lessonId).data();

        if (quizSessionRepository.existsByQuizLessonIdAndUserIdAndStatus(lessonId, userId, QuizSessionStatus.IN_PROGRESS)) {
            throw new BadRequestException(ErrorCode.QUIZ_SESSION_ALREADY_IN_PROGRESS, "You already have an in-progress quiz session for this lesson");
        }

        Integer maxAttempt = quizLessonResponse.maxAttempt();
        if (maxAttempt != null && maxAttempt <= quizSessionRepository.countByQuizLessonIdAndUserId(lessonId, userId)) {
            throw new BadRequestException(ErrorCode.QUIZ_ATTEMPT_LIMIT_REACHED, "You have reached the maximum number of attempts for this quiz lesson");
        }

        QuizSession quizSession = quizSessionRepository.save(
                QuizSession.builder()
                        .courseId(courseId)
                        .userId(userId)
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusMinutes(quizLessonResponse.duration()))
                        .quizLessonId(lessonId)
                        .status(QuizSessionStatus.IN_PROGRESS)
                        .isReviewAllowed(quizLessonResponse.isReviewAllowed())
                        .isShowAnswersOnReview(quizLessonResponse.isShowAnswersOnReview())
                        .build());

        createQuizSessionQuestions(
                quizSession,
                quizLessonResponse.numberOfQuestionPerQuizSession(),
                quizLessonResponse.questions(),
                Boolean.TRUE.equals(quizLessonResponse.shuffleQuestions()),
                Boolean.TRUE.equals(quizLessonResponse.shuffleOptions()));

        return quizSessionMapper.toDto(quizSession);
    }

    private void createQuizSessionQuestions(
            QuizSession quizSession,
            Integer numberOfQuestionPerQuizSession,
            List<QuizQuestionResponse> questions,
            boolean shuffleQuestions,
            boolean shuffleOptions) {
        List<QuizQuestionResponse> pool = new ArrayList<>(questions);
        if (shuffleQuestions)
            Collections.shuffle(pool);
        int requestedQuestionCount = numberOfQuestionPerQuizSession != null
                ? numberOfQuestionPerQuizSession
                : pool.size();
        List<QuizQuestionResponse> selected = pool.subList(0, Math.min(requestedQuestionCount, pool.size()));

        List<QuizSessionQuestion> sessionQuestions = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            QuizQuestionResponse q = selected.get(i);
            sessionQuestions.add(QuizSessionQuestion.builder()
                    .quizSessionId(quizSession.getId())
                    .questionOrder(i + 1)
                    .questionType(q.questionType())
                    .questionId(q.id())
                    .questionText(q.questionText())
                    .scoringMethod(q.scoringMethod())
                    .correctAnswer(snapshotBuilder.buildCorrectAnswer(q))
                    .optionsSnapshot(snapshotBuilder.buildOptionsSnapshot(q, shuffleOptions))
                    .build());
        }

        quizSession.setQuestions(quizSessionQuestionRepository.saveAll(sessionQuestions));
    }

    @Override
    @Transactional
    public void chooseQuizSessionQuestion(String quizSessionId, ChooseQuizAnswerRequest request) {
        QuizSession quizSession = quizSessionRepository.findById(quizSessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));
        if (quizSession.getStatus() != QuizSessionStatus.IN_PROGRESS) {
            throw new BadRequestException(ErrorCode.QUIZ_SESSION_NOT_IN_PROGRESS, "Quiz session is not in progress");
        }
        if (quizSession.getEndTime().isBefore(LocalDateTime.now())) {
            submitQuizSession(quizSessionId, new SubmitQuizSessionRequest(Collections.emptyList()));
            throw new BadRequestException(ErrorCode.QUIZ_SESSION_EXPIRED, "Quiz session has expired and was automatically submitted");
        }

        quizSessionQuestionRepository.findByQuizSessionIdAndQuestionId(quizSessionId, request.questionId())
                .ifPresentOrElse(
                        q -> {
                            q.setUserAnswer(request.userAnswer());
                            quizSessionQuestionRepository.save(q);
                        },
                        () -> {
                            throw new NotFoundException("Quiz session question not found");
                        });
    }

    @Transactional
    @Override
    public QuizSessionResponse submitQuizSession(String quizSessionId, SubmitQuizSessionRequest request) {
        QuizSession quizSession = quizSessionRepository.findById(quizSessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));

        if (quizSession.getStatus() != QuizSessionStatus.IN_PROGRESS) {
            throw new BadRequestException(ErrorCode.QUIZ_SESSION_NOT_IN_PROGRESS, "Quiz session is not in progress");
        }
        
        boolean isExpired = quizSession.getEndTime().isBefore(LocalDateTime.now());
        
        Map<String, ChooseQuizAnswerRequest> answerMap = !isExpired
                ? buildAnswerMap(request)
                : Collections.emptyMap();

        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findAllByQuizSessionId(quizSessionId, Pageable.unpaged()).getContent();
        questions.forEach(q -> {
            ChooseQuizAnswerRequest lastAnswer = answerMap.get(q.getQuestionId());
            if (lastAnswer != null)
                q.setUserAnswer(lastAnswer.userAnswer());
        });

        boolean hasEssay = false;
        double totalScore = 0.0;
        int correctCount = 0;

        for (QuizSessionQuestion q : questions) {
            if (q.getQuestionType() == QuizQuestionType.ESSAY) {
                hasEssay = true;
            }
            IQuestionEvaluator evaluator = questionEvaluatorFactory.resolve(q);
            double fraction = evaluator.evaluate(q);
            q.setScore(fraction);
            totalScore += fraction;
            if (fraction >= 1.0) {
                correctCount++;
            }
            log.debug("Graded question {} | type={} | method={} | score={}",
                    q.getQuestionId(), q.getQuestionType(), q.getScoringMethod(), fraction);
        }

        quizSessionQuestionRepository.saveAll(questions);

        quizSession.setStatus(hasEssay ? QuizSessionStatus.PENDING_GRADE : QuizSessionStatus.SUBMITTED);
        quizSessionRepository.save(quizSession);

        double rawScore = questions.isEmpty() ? 0.0 : totalScore / questions.size() * 10.0;

        quizSession.setQuizSessionSubmission(quizSessionSubmissionRepository.save(
                QuizSessionSubmission.builder()
                        .userId(quizSession.getUserId())
                        .quizSessionId(quizSession.getId())
                        .score(rawScore)
                        .submissionTime(LocalDateTime.now())
                        .totalCorrectAnswers(correctCount)
                        .build()));

        if (!hasEssay) {
            double effectiveScore = quizScoreAggregator.aggregateScore(
                    quizSession.getCourseId(),
                    quizSession.getUserId(),
                    quizSession.getQuizLessonId());

            log.info("Quiz session {} graded. rawScore={} effectiveScore={}", quizSessionId, rawScore, effectiveScore);

            boolean isPassed = effectiveScore >= PASSING_SCORE;

            LearningItemProgressUpdateResult result = learningProgressService.updateLearningItemProgress(
                    quizSession.getUserId(),
                    quizSession.getQuizLessonId(),
                    new UpdateLearningItemRequest(true, isPassed, effectiveScore));

            if (result.passedTransition()) {
                dailyGoalService.recordProgress(quizSession.getUserId(), DailyGoalType.QUIZZES_PASSED, 1);
            }
        } else {
            log.info("Quiz session {} pending essay grading. rawScore={}", quizSessionId, rawScore);
        }

        return quizSessionMapper.toDto(quizSession);
    }

    @Override
    @Transactional
    public QuizSessionResponse gradeEssay(String sessionId, GradeEssayRequest request) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));

        if (session.getStatus() != QuizSessionStatus.PENDING_GRADE) {
            throw new BadRequestException(ErrorCode.QUIZ_SESSION_NOT_PENDING_GRADING, "Quiz session is not pending essay grading");
        }

        Map<String, Double> scoreMap = buildEssayScoreMap(request);

        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findAllEssayByQuizSessionId(sessionId);
        questions.stream()
                .filter(q -> q.getQuestionType() == QuizQuestionType.ESSAY)
                .forEach(q -> {
                    Double assignedScore = scoreMap.get(q.getQuestionId());
                    if (assignedScore == null) {
                        throw new BadRequestException(ErrorCode.QUIZ_ESSAY_SCORE_INVALID, "Missing score for essay question: " + q.getQuestionId());
                    }
                    q.setScore(assignedScore / 10.0);
                });

        quizSessionQuestionRepository.saveAll(questions);

        List<QuizSessionQuestion> allQuestions = quizSessionQuestionRepository
                .findAllByQuizSessionId(sessionId, Pageable.unpaged())
                .getContent();

        double totalFraction = allQuestions.stream()
                .mapToDouble(q -> q.getScore() != null ? q.getScore() : 0.0)
                .sum();
        double rawScore = allQuestions.isEmpty() ? 0.0 : (totalFraction / allQuestions.size()) * 10.0;
        int correctCount = (int) allQuestions.stream()
                .filter(q -> q.getScore() != null && q.getScore() >= 1.0)
                .count();

        session.setStatus(QuizSessionStatus.GRADED);
        quizSessionRepository.save(session);

        QuizSessionSubmission submission = quizSessionSubmissionRepository
                .findByQuizSessionId(sessionId)
                .orElseThrow(() -> new BadRequestException("Quiz session submission not found"));
        submission.setScore(rawScore);
        submission.setTotalCorrectAnswers(correctCount);
        session.setQuizSessionSubmission(quizSessionSubmissionRepository.save(submission));

        double effectiveScore = quizScoreAggregator.aggregateScore(session.getCourseId(), session.getUserId(), session.getQuizLessonId());
        log.info("Essay graded for session {}. rawScore={} effectiveScore={}", sessionId, rawScore, effectiveScore);

        boolean isPassed = effectiveScore >= PASSING_SCORE;

        LearningItemProgressUpdateResult result = learningProgressService.updateLearningItemProgress(
                session.getUserId(),
                session.getQuizLessonId(),
                new UpdateLearningItemRequest(true, isPassed, effectiveScore));

        if (result.passedTransition()) {
            dailyGoalService.recordProgress(session.getUserId(), DailyGoalType.QUIZZES_PASSED, 1);
        }

        return quizSessionMapper.toDto(session);
    }

    @Override
    public List<QuizQuestionAnalyticsResponse> getQuizAnalytics(String courseId, String lessonId) {
        courseService.getQuizLessonById(courseId, lessonId);
        return quizSessionQuestionRepository.getQuizAnalyticsByQuizId(lessonId)
                .stream()
                .map(obj -> new QuizQuestionAnalyticsResponse(
                        (String) obj[0],
                        ((Number) obj[1]).intValue(),
                        ((Number) obj[2]).intValue(),
                        ((Number) obj[1]).doubleValue() == 0 ? 0.0 : ((Number) obj[2]).doubleValue() / ((Number) obj[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRateString = "${app.quiz.auto-submit.rate:60000}")
    @Transactional
    public void autoSubmitExpiredSessions() {
        List<QuizSession> expiredSessions = quizSessionRepository.findExpiredSessions(QuizSessionStatus.IN_PROGRESS, LocalDateTime.now());
        if (!expiredSessions.isEmpty()) {
            log.info("Found {} expired quiz sessions. Starting auto-submission.", expiredSessions.size());
        }
        for (QuizSession session : expiredSessions) {
            try {
                // Submit empty request, the internal logic will use currently saved answers since it's already expired
                submitQuizSession(session.getId(), new SubmitQuizSessionRequest(Collections.emptyList()));
                log.info("Auto-submitted expired quiz session: {}", session.getId());
            } catch (Exception e) {
                log.error("Failed to auto-submit expired quiz session: {}", session.getId(), e);
            }
        }
    }

    private boolean isReviewStatus(QuizSessionStatus status) {
        return status == QuizSessionStatus.SUBMITTED || status == QuizSessionStatus.GRADED;
    }

    private Map<String, ChooseQuizAnswerRequest> buildAnswerMap(SubmitQuizSessionRequest request) {
        if (request == null || request.answers() == null || request.answers().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ChooseQuizAnswerRequest> answerMap = new HashMap<>();
        for (ChooseQuizAnswerRequest answer : request.answers()) {
            if (answer == null) {
                throw new BadRequestException(ErrorCode.QUIZ_ANSWER_INVALID, "answers must not contain null items");
            }
            if (answerMap.putIfAbsent(answer.questionId(), answer) != null) {
                throw new BadRequestException(ErrorCode.QUIZ_ANSWER_INVALID, "Duplicate answer for questionId: " + answer.questionId());
            }
        }
        return answerMap;
    }

    private Map<String, Double> buildEssayScoreMap(GradeEssayRequest request) {
        if (request == null || request.scores() == null || request.scores().isEmpty()) {
            throw new BadRequestException(ErrorCode.QUIZ_ESSAY_SCORE_INVALID, "Essay scores are required");
        }
        Map<String, Double> scoreMap = new HashMap<>();
        for (GradeEssayRequest.EssayQuestionScore score : request.scores()) {
            if (score == null) {
                throw new BadRequestException(ErrorCode.QUIZ_ESSAY_SCORE_INVALID, "Essay scores must not contain null items");
            }
            if (scoreMap.putIfAbsent(score.questionId(), score.score()) != null) {
                throw new BadRequestException(ErrorCode.QUIZ_ESSAY_SCORE_INVALID, "Duplicate score for essay question: " + score.questionId());
            }
        }
        return scoreMap;
    }

    private void validatePageRequest(int page, int size) {
        if (page < 1) {
            throw new BadRequestException(ErrorCode.INVALID_PAGINATION, "page must be greater than or equal to 1");
        }
        if (size < 1) {
            throw new BadRequestException(ErrorCode.INVALID_PAGINATION, "size must be greater than or equal to 1");
        }
    }
}
