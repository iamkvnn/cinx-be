package com.cinx.learning.messaging;

import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.consts.ScoringMode;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.messaging.event.QuizSyncEvent;
import com.cinx.learning.model.QuizScoreHistory;
import com.cinx.learning.model.QuizSession;
import com.cinx.learning.model.QuizSessionQuestion;
import com.cinx.learning.model.QuizSessionSubmission;
import com.cinx.learning.repository.QuizScoreHistoryRepository;
import com.cinx.learning.repository.QuizSessionQuestionRepository;
import com.cinx.learning.repository.QuizSessionSubmissionRepository;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.quiz.evaluator.IQuestionEvaluator;
import com.cinx.learning.service.quiz.evaluator.QuestionEvaluatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizSyncConsumer {

    private final QuizSessionQuestionRepository quizSessionQuestionRepository;
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final QuizScoreHistoryRepository quizScoreHistoryRepository;
    private final ILearningProgressService learningProgressService;

    @RabbitListener(queues = "learning.quiz-sync.queue", containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void receiveQuizSyncEvent(QuizSyncEvent event) {
        log.info("Received QuizSyncEvent: quizLessonId={}", event.getQuizLessonId());

        Map<String, QuizSyncEvent.QuestionSnapshot> snapshotMap = event.getQuestions().stream()
                .collect(Collectors.toMap(QuizSyncEvent.QuestionSnapshot::getQuestionId, s -> s));

        List<String> affectedQuestionIds = snapshotMap.keySet().stream().toList();

        List<QuizSessionQuestion> affectedRows = quizSessionQuestionRepository
                .findAllByQuizLessonIdAndQuestionIdIn(event.getQuizLessonId(), affectedQuestionIds);

        if (affectedRows.isEmpty()) {
            log.info("No QuizSessionQuestion rows found for affected questions in quizLessonId={}",
                    event.getQuizLessonId());
            return;
        }

        List<QuizSessionQuestion> updated = affectedRows.stream()
                .filter(q -> {
                    QuizSyncEvent.QuestionSnapshot snap = snapshotMap.get(q.getQuestionId());
                    if (snap == null) return false;
                    boolean changed = false;
                    if (!Objects.equals(q.getCorrectAnswer(), snap.getCorrectAnswer())) {
                        q.setCorrectAnswer(snap.getCorrectAnswer());
                        changed = true;
                    }
                    if (snap.getScoringMethod() != null
                            && !Objects.equals(q.getScoringMethod(), snap.getScoringMethod())) {
                        q.setScoringMethod(snap.getScoringMethod());
                        changed = true;
                    }
                    return changed;
                })
                .toList();

        if (!updated.isEmpty()) {
            quizSessionQuestionRepository.saveAll(updated);
        }
        log.info("Updated {} QuizSessionQuestion rows for quizLessonId={}", updated.size(), event.getQuizLessonId());

        Map<String, QuizSession> affectedSessions = updated.stream()
                .map(QuizSessionQuestion::getQuizSession)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        QuizSession::getId,
                        s -> s,
                        (a, b) -> a
                ));

        List<QuizSession> regradableSessions = affectedSessions.values().stream()
                .filter(s -> s.getStatus() == QuizSessionStatus.SUBMITTED
                          || s.getStatus() == QuizSessionStatus.GRADED
                          || s.getStatus() == QuizSessionStatus.PENDING_GRADE)
                .toList();

        log.info("Affected sessions: {} total, {} eligible for regrade",
                affectedSessions.size(), regradableSessions.size());

        ScoringMode scoringMode = event.getScoringMode() != null ? event.getScoringMode() : ScoringMode.HIGHEST;

        int regradeCount = 0;
        for (QuizSession session : regradableSessions) {
            List<QuizSessionQuestion> allSessionQuestions = quizSessionQuestionRepository
                    .findAllByQuizSessionId(session.getId(), Pageable.unpaged())
                    .getContent();

            if (allSessionQuestions.isEmpty()) continue;

            double totalFraction = 0.0;
            for (QuizSessionQuestion q : allSessionQuestions) {
                IQuestionEvaluator evaluator = QuestionEvaluatorFactory.resolve(q);
                double fraction = evaluator.evaluate(q);
                q.setScore(fraction);
                totalFraction += fraction;
            }
            quizSessionQuestionRepository.saveAll(allSessionQuestions);

            double newRawScore = (totalFraction / allSessionQuestions.size()) * 10.0;
            int correctCount = (int) allSessionQuestions.stream()
                    .filter(q -> q.getScore() != null && q.getScore() >= 1.0)
                    .count();

            QuizSessionSubmission submission = quizSessionSubmissionRepository
                    .findByQuizSessionId(session.getId())
                    .orElseGet(() -> QuizSessionSubmission.builder()
                            .quizSessionId(session.getId())
                            .submissionTime(LocalDateTime.now())
                            .build());

            double oldScore = submission.getScore() != null ? submission.getScore() : 0.0;
            submission.setScore(newRawScore);
            submission.setTotalCorrectAnswers(correctCount);
            quizSessionSubmissionRepository.save(submission);

            if (Math.abs(newRawScore - oldScore) > 0.0001) {
                quizScoreHistoryRepository.save(QuizScoreHistory.builder()
                        .quizSessionId(session.getId())
                        .oldScore(oldScore)
                        .newScore(newRawScore)
                        .reason(event.getChangeReason())
                        .gradedAt(LocalDateTime.now())
                        .build());
                regradeCount++;
                log.info("Regraded session {} | old={} new={}", session.getId(), oldScore, newRawScore);
            }

            updateLearningProgress(session.getUserId(), session.getQuizLessonId(), scoringMode);
        }

        log.info("Regrade complete for quizLessonId={}. {} sessions had score changes.",
                event.getQuizLessonId(), regradeCount);
    }

    private void updateLearningProgress(String userId, String quizLessonId, ScoringMode scoringMode) {
        List<QuizSessionSubmission> allSubmissions =
                quizSessionSubmissionRepository.findAllByUserIdAndQuizLessonId(userId, quizLessonId);

        List<Double> scores = allSubmissions.stream()
                .map(QuizSessionSubmission::getScore)
                .filter(Objects::nonNull)
                .toList();

        if (scores.isEmpty()) return;

        double effectiveScore = switch (scoringMode) {
            case HIGHEST -> scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case LATEST  -> scores.getLast();
            case FIRST   -> scores.getFirst();
            case AVERAGE -> scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        };

        learningProgressService.updateLearningItemProgress(
                userId,
                quizLessonId,
                new UpdateLearningItemRequest(true, effectiveScore >= 5.0, effectiveScore)
        );

        log.info("Updated learning progress for userId={} quizLessonId={} effectiveScore={}",
                userId, quizLessonId, effectiveScore);
    }
}
