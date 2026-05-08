package com.cinx.learning.service.quiz;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.consts.ScoringMode;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.GradeEssayRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.*;
import com.cinx.learning.mapper.QuizSessionMapper;
import com.cinx.learning.mapper.QuizSessionQuestionMapper;
import com.cinx.learning.model.QuizSession;
import com.cinx.learning.model.QuizSessionQuestion;
import com.cinx.learning.model.QuizSessionSubmission;
import com.cinx.learning.repository.QuizSessionQuestionRepository;
import com.cinx.learning.repository.QuizSessionRepository;
import com.cinx.learning.repository.QuizSessionSubmissionRepository;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.quiz.evaluator.IQuestionEvaluator;
import com.cinx.learning.service.quiz.evaluator.QuestionEvaluatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {

    private final QuizSessionRepository quizSessionRepository;
    private final CourseService courseService;
    private final QuizSessionMapper quizSessionMapper;
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final QuizSessionQuestionMapper quizSessionQuestionMapper;
    private final QuizSessionQuestionRepository quizSessionQuestionRepository;
    private final ILearningProgressService learningProgressService;

    @Override
    public Page<QuizSessionResponse> getQuizSessions(String userId, String quizLessonId, int page, int size) {
        return quizSessionRepository.findAllByQuizLessonId(quizLessonId, userId, PageRequest.of(page - 1, size))
                .map(quizSessionMapper::toDto);
    }

    @Override
    public QuizSessionResponse getQuizSession(String id) {
        return quizSessionRepository.findById(id)
                .map(quizSessionMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));
    }

    @Override
    public Page<QuizSessionQuestionResponse> getQuizSessionQuestions(String quizSessionId, int page, int size) {
        QuizSession quizSession = quizSessionRepository.findById(quizSessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));

        if (quizSession.getStatus() == QuizSessionStatus.SUBMITTED) {
            if (Boolean.FALSE.equals(quizSession.getIsReviewAllowed())) {
                throw new BadRequestException("Review is not allowed for this quiz session");
            }
        }

        return quizSessionQuestionRepository.findAllByQuizSessionId(quizSessionId, PageRequest.of(page - 1, size))
                .map(quizSessionQuestionMapper::toDto)
                .map(dto -> {
                    boolean inProgress = quizSession.getStatus() == QuizSessionStatus.IN_PROGRESS;
                    boolean hideAnswers = Boolean.FALSE.equals(quizSession.getIsShowAnswersOnReview());
                    if (inProgress || hideAnswers) {
                        return new QuizSessionQuestionResponse(
                                dto.id(),
                                dto.quizSessionId(),
                                dto.questionId(),
                                dto.questionType(),
                                dto.questionOrder(),
                                dto.userAnswer(),
                                null,
                                null,
                                null,
                                dto.scoringMethod()
                        );
                    }
                    return dto;
                });
    }

    @Transactional
    @Override
    public QuizSessionResponse createQuizSession(String userId, String quizLessonId) {
        QuizLessonResponse quizLessonResponse = courseService.getQuizLessonById(quizLessonId).data();

        Integer maxAttempt = quizLessonResponse.maxAttempt();
        if (maxAttempt != null && maxAttempt <= quizSessionRepository.countByQuizLessonId(quizLessonId)) {
            throw new BadRequestException("You have reached the maximum number of attempts for this quiz lesson");
        }

        QuizSession quizSession = quizSessionRepository.save(
                QuizSession.builder()
                        .userId(userId)
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now().plusMinutes(quizLessonResponse.duration()))
                        .quizLessonId(quizLessonId)
                        .status(QuizSessionStatus.IN_PROGRESS)
                        .isReviewAllowed(quizLessonResponse.isReviewAllowed())
                        .isShowAnswersOnReview(quizLessonResponse.isShowAnswersOnReview())
                        .build()
        );

        createQuizSessionQuestions(
                quizSession,
                quizLessonResponse.numberOfQuestionPerQuizSession(),
                quizLessonResponse.questions(),
                Boolean.TRUE.equals(quizLessonResponse.shuffleQuestions())
        );

        return quizSessionMapper.toDto(quizSession);
    }

    private void createQuizSessionQuestions(
            QuizSession quizSession,
            Integer numberOfQuestionPerQuizSession,
            List<QuizQuestionResponse> questions,
            boolean shuffleQuestions
    ) {
        List<QuizQuestionResponse> pool = new ArrayList<>(questions);
        Collections.shuffle(pool);
        List<QuizQuestionResponse> selected = pool.subList(0, Math.min(numberOfQuestionPerQuizSession, pool.size()));

        List<QuizSessionQuestion> sessionQuestions = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            QuizQuestionResponse q = selected.get(i);
            sessionQuestions.add(QuizSessionQuestion.builder()
                    .quizSessionId(quizSession.getId())
                    .questionOrder(i + 1)
                    .questionType(q.questionType())
                    .questionId(q.id())
                    .scoringMethod(q.scoringMethod())
                    .correctAnswer(buildCorrectAnswer(q))
                    .build());
        }

        quizSession.setQuestions(quizSessionQuestionRepository.saveAll(sessionQuestions));
    }

    private String buildCorrectAnswer(QuizQuestionResponse q) {
        return switch (q.questionType()) {
            case MATCHING -> q.options().stream()
                    .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                    .sorted(Comparator.comparing(QuizOptionResponse::optionOrder))
                    .map(o -> o.id() + ":" + (o.matchText() != null ? o.matchText() : ""))
                    .collect(Collectors.joining(","));
            case SHORT_TEXT, ESSAY -> q.options().stream()
                    .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                    .map(QuizOptionResponse::optionText)
                    .sorted()
                    .collect(Collectors.joining(","));
            default -> // SINGLE_CHOICE, MULTI_CHOICE, ORDERING
                    q.options().stream()
                    .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                    .map(QuizOptionResponse::id)
                    .sorted()
                    .collect(Collectors.joining(","));
        };
    }

    @Override
    public void chooseQuizSessionQuestion(String quizSessionId, ChooseQuizAnswerRequest request) {
        quizSessionQuestionRepository.findByQuizSessionIdAndQuestionId(quizSessionId, request.questionId())
                .ifPresentOrElse(
                        quizSessionQuestion -> {
                            quizSessionQuestion.setUserAnswer(request.userAnswer());
                            quizSessionQuestionRepository.save(quizSessionQuestion);
                        },
                        () -> { throw new NotFoundException("Quiz session question not found"); }
                );
    }

    @Transactional
    @Override
    public QuizSessionResponse submitQuizSession(String quizSessionId, SubmitQuizSessionRequest request) {
        QuizSession quizSession = quizSessionRepository.findById(quizSessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));

        if (quizSession.getStatus() != QuizSessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Quiz session is not in progress");
        }
        if (quizSession.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Quiz session has expired");
        }

        Map<String, ChooseQuizAnswerRequest> answerMap = request.answers() == null
                ? Collections.emptyMap()
                : request.answers().stream()
                        .collect(Collectors.toMap(ChooseQuizAnswerRequest::questionId, a -> a));

        List<QuizSessionQuestion> questions = quizSession.getQuestions();
        questions.forEach(q -> {
            ChooseQuizAnswerRequest lastAnswer = answerMap.get(q.getQuestionId());
            if (lastAnswer != null) {
                q.setUserAnswer(lastAnswer.userAnswer());
            }
        });

        boolean hasEssay = false;
        double totalScore = 0.0;

        for (QuizSessionQuestion q : questions) {
            if (q.getQuestionType() == QuizQuestionType.ESSAY) {
                hasEssay = true;
                q.setScore(0.0);
                continue;
            }
            IQuestionEvaluator evaluator = QuestionEvaluatorFactory.resolve(q);
            double fraction = evaluator.evaluate(q);
            q.setScore(fraction);
            totalScore += fraction;

            log.debug("Graded question {} | type={} | method={} | score={}",
                    q.getQuestionId(), q.getQuestionType(), q.getScoringMethod(), fraction);
        }

        quizSessionQuestionRepository.saveAll(questions);

        if (hasEssay) {
            quizSession.setStatus(QuizSessionStatus.PENDING_GRADE);
            quizSessionRepository.save(quizSession);
            log.info("Quiz session {} set to PENDING_GRADE (contains ESSAY questions)", quizSessionId);
        } else {
            quizSession.setStatus(QuizSessionStatus.GRADED);
            quizSessionRepository.save(quizSession);

            double rawScore = questions.isEmpty() ? 0.0 : (totalScore / questions.size()) * 10.0;
            int correctCount = (int) questions.stream()
                    .filter(q -> q.getScore() != null && q.getScore() >= 1.0)
                    .count();

            quizSession.setQuizSessionSubmission(quizSessionSubmissionRepository.save(
                    QuizSessionSubmission.builder()
                            .quizSessionId(quizSession.getId())
                            .score(rawScore)
                            .submissionTime(LocalDateTime.now())
                            .totalCorrectAnswers(correctCount)
                            .build()
            ));

            double effectiveScore = aggregateScore(
                    quizSession.getUserId(),
                    quizSession.getQuizLessonId(),
                    quizSession.getQuizSessionSubmission()
            );

            log.info("Quiz session {} graded. rawScore={} effectiveScore={}", quizSessionId, rawScore, effectiveScore);

            learningProgressService.updateLearningItemProgress(
                    quizSession.getUserId(),
                    quizSession.getQuizLessonId(),
                    new UpdateLearningItemRequest(true, effectiveScore >= 5.0, effectiveScore)
            );
        }

        return quizSessionMapper.toDto(quizSession);
    }

    private double aggregateScore(String userId, String quizLessonId, QuizSessionSubmission currentSubmission) {
        List<QuizSessionSubmission> allSubmissions =
                quizSessionSubmissionRepository.findAllByUserIdAndQuizLessonId(userId, quizLessonId);

        if (allSubmissions.isEmpty()) {
            return currentSubmission.getScore();
        }

        ScoringMode scoringMode;
        try {
            scoringMode = courseService.getQuizLessonById(quizLessonId).data().scoringMode();
        } catch (Exception e) {
            log.warn("Could not fetch scoringMode for quizLessonId={}, defaulting to HIGHEST", quizLessonId);
            scoringMode = ScoringMode.HIGHEST;
        }
        if (scoringMode == null) scoringMode = ScoringMode.HIGHEST;

        List<Double> scores = allSubmissions.stream()
                .map(QuizSessionSubmission::getScore)
                .filter(Objects::nonNull)
                .toList();

        return switch (scoringMode) {
            case HIGHEST -> scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case LATEST  -> scores.getLast(); // list is sorted ASC; last = most recent
            case FIRST   -> scores.getFirst();
            case AVERAGE -> scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizSessionResponse> getPendingGradeSessions(String quizLessonId, int page, int size) {
        return quizSessionRepository
                .findAllByQuizLessonIdAndStatus(quizLessonId, QuizSessionStatus.PENDING_GRADE, PageRequest.of(page - 1, size))
                .map(quizSessionMapper::toDto);
    }

    @Override
    @Transactional
    public QuizSessionResponse gradeEssay(String sessionId, GradeEssayRequest request) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));

        if (session.getStatus() != QuizSessionStatus.PENDING_GRADE) {
            throw new BadRequestException("Quiz session is not pending essay grading");
        }

        Map<String, Double> scoreMap = request.scores().stream()
                .collect(Collectors.toMap(
                        GradeEssayRequest.EssayQuestionScore::questionId,
                        GradeEssayRequest.EssayQuestionScore::score
                ));

        List<QuizSessionQuestion> questions = session.getQuestions();

        questions.stream()
                .filter(q -> q.getQuestionType() == QuizQuestionType.ESSAY)
                .forEach(q -> {
                    Double assignedScore = scoreMap.get(q.getQuestionId());
                    if (assignedScore == null) {
                        throw new BadRequestException("Missing score for essay question: " + q.getQuestionId());
                    }
                    if (assignedScore < 0.0 || assignedScore > 1.0) {
                        throw new BadRequestException("Score must be in range [0.0, 1.0] for question: " + q.getQuestionId());
                    }
                    q.setScore(assignedScore);
                });

        quizSessionQuestionRepository.saveAll(questions);

        double totalFraction = questions.stream()
                .mapToDouble(q -> q.getScore() != null ? q.getScore() : 0.0)
                .sum();
        double rawScore = questions.isEmpty() ? 0.0 : (totalFraction / questions.size()) * 10.0;
        int correctCount = (int) questions.stream()
                .filter(q -> q.getScore() != null && q.getScore() >= 1.0)
                .count();

        session.setStatus(QuizSessionStatus.GRADED);
        quizSessionRepository.save(session);

        QuizSessionSubmission submission = quizSessionSubmissionRepository
                .findByQuizSessionId(sessionId)
                .orElseGet(() -> QuizSessionSubmission.builder()
                        .quizSessionId(sessionId)
                        .submissionTime(LocalDateTime.now())
                        .build());
        submission.setScore(rawScore);
        submission.setTotalCorrectAnswers(correctCount);
        session.setQuizSessionSubmission(quizSessionSubmissionRepository.save(submission));

        double effectiveScore = aggregateScore(session.getUserId(), session.getQuizLessonId(), submission);

        log.info("Essay graded for session {}. rawScore={} effectiveScore={}", sessionId, rawScore, effectiveScore);

        learningProgressService.updateLearningItemProgress(
                session.getUserId(),
                session.getQuizLessonId(),
                new UpdateLearningItemRequest(true, effectiveScore >= 5.0, effectiveScore)
        );

        return quizSessionMapper.toDto(session);
    }
}
