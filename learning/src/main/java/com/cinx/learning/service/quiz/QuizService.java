package com.cinx.learning.service.quiz;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
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
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.quiz.evaluator.IQuestionEvaluator;
import com.cinx.learning.service.quiz.evaluator.QuestionEvaluatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {

    private final QuizScoreAggregator quizScoreAggregator;
    private final QuestionEvaluatorFactory questionEvaluatorFactory;
    private final QuizSessionRepository quizSessionRepository;
    private final CourseService courseService;
    private final QuizSessionMapper quizSessionMapper;
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final QuizSessionQuestionRepository quizSessionQuestionRepository;
    private final ILearningProgressService learningProgressService;
    private final QuizSnapshotBuilder snapshotBuilder;

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

        boolean inProgress = quizSession.getStatus() == QuizSessionStatus.IN_PROGRESS;
        boolean hideAnswers = Boolean.FALSE.equals(quizSession.getIsShowAnswersOnReview());

        return quizSessionQuestionRepository
                .findAllByQuizSessionId(quizSessionId, PageRequest.of(page - 1, size))
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
    public QuizSessionResponse createQuizSession(String userId, String quizLessonId) {
        QuizLessonResponse quizLessonResponse = courseService.getQuizLessonById(quizLessonId).data();

        Integer maxAttempt = quizLessonResponse.maxAttempt();
        if (maxAttempt != null && maxAttempt <= quizSessionRepository.countByQuizLessonIdAndUserId(quizLessonId, userId)) {
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
        List<QuizQuestionResponse> selected = pool.subList(0, Math.min(numberOfQuestionPerQuizSession, pool.size()));

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
    public void chooseQuizSessionQuestion(String quizSessionId, ChooseQuizAnswerRequest request) {
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
            throw new BadRequestException("Quiz session is not in progress");
        }
        if (quizSession.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Quiz session has expired");
        }

        Map<String, ChooseQuizAnswerRequest> answerMap = request.answers() == null
                ? Collections.emptyMap()
                : request.answers().stream()
                        .collect(Collectors.toMap(ChooseQuizAnswerRequest::questionId, a -> a));

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

        double rawScore = totalScore / questions.size() * 10.0;

        quizSession.setQuizSessionSubmission(quizSessionSubmissionRepository.save(
                QuizSessionSubmission.builder()
                        .quizSessionId(quizSession.getId())
                        .score(rawScore)
                        .submissionTime(LocalDateTime.now())
                        .totalCorrectAnswers(correctCount)
                        .build()));

        double effectiveScore = quizScoreAggregator.aggregateScore(
                quizSession.getUserId(),
                quizSession.getQuizLessonId());

        log.info("Quiz session {} graded. rawScore={} effectiveScore={}", quizSessionId, rawScore, effectiveScore);

        learningProgressService.updateLearningItemProgress(
                quizSession.getUserId(),
                quizSession.getQuizLessonId(),
                new UpdateLearningItemRequest(true, effectiveScore >= 5.0, effectiveScore));

        return quizSessionMapper.toDto(quizSession);
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
                        GradeEssayRequest.EssayQuestionScore::score));

        List<QuizSessionQuestion> questions = quizSessionQuestionRepository.findAllEssayByQuizSessionId(sessionId);
        questions.stream()
                .filter(q -> q.getQuestionType() == QuizQuestionType.ESSAY)
                .forEach(q -> {
                    Double assignedScore = scoreMap.get(q.getQuestionId());
                    if (assignedScore == null) {
                        throw new BadRequestException("Missing score for essay question: " + q.getQuestionId());
                    }
                    q.setScore(assignedScore);
                });

        quizSessionQuestionRepository.saveAll(questions);

        double totalFraction = questions.stream()
                .mapToDouble(QuizSessionQuestion::getScore)
                .sum();
        double rawScore = questions.isEmpty() ? 0.0 : (totalFraction / questions.size()) * 10.0;
        int correctCount = (int) questions.stream()
                .filter(q -> q.getScore() != null && q.getScore() >= 1.0)
                .count();

        session.setStatus(QuizSessionStatus.GRADED);
        quizSessionRepository.save(session);

        QuizSessionSubmission submission = quizSessionSubmissionRepository
                .findByQuizSessionId(sessionId)
                .orElseThrow(() -> new BadRequestException("Quiz session submission not found"));
        submission.setScore(submission.getScore() + rawScore);
        submission.setTotalCorrectAnswers(submission.getTotalCorrectAnswers() + correctCount);
        session.setQuizSessionSubmission(quizSessionSubmissionRepository.save(submission));

        double effectiveScore = quizScoreAggregator.aggregateScore(session.getUserId(), session.getQuizLessonId());
        log.info("Essay graded for session {}. rawScore={} effectiveScore={}", sessionId, rawScore, effectiveScore);

        learningProgressService.updateLearningItemProgress(
                session.getUserId(),
                session.getQuizLessonId(),
                new UpdateLearningItemRequest(true, effectiveScore >= 5.0, effectiveScore));

        return quizSessionMapper.toDto(session);
    }

    @Override
    public List<QuizQuestionAnalyticsResponse> getQuizAnalytics(String quizId) {
        return quizSessionQuestionRepository.getQuizAnalyticsByQuizId(quizId)
                .stream()
                .map(obj -> new QuizQuestionAnalyticsResponse(
                        (String) obj[0],
                        ((Number) obj[1]).intValue(),
                        ((Number) obj[2]).intValue(),
                        ((Number) obj[1]).doubleValue() == 0 ? 0.0 : ((Number) obj[2]).doubleValue() / ((Number) obj[1]).doubleValue()
                ))
                .collect(Collectors.toList());
    }
}
