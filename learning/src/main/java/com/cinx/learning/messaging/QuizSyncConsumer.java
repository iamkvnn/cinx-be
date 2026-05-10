package com.cinx.learning.messaging;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.consts.ScoringMode;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.QuizQuestionResponse;
import com.cinx.learning.messaging.event.QuizSyncEvent;
import com.cinx.learning.model.QuizScoreHistory;
import com.cinx.learning.model.QuizSession;
import com.cinx.learning.model.QuizSessionQuestion;
import com.cinx.learning.model.QuizSessionSubmission;
import com.cinx.learning.repository.QuizScoreHistoryRepository;
import com.cinx.learning.repository.QuizSessionQuestionRepository;
import com.cinx.learning.repository.QuizSessionSubmissionRepository;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.quiz.QuizScoreAggregator;
import com.cinx.learning.service.quiz.QuizSnapshotBuilder;
import com.cinx.learning.service.quiz.evaluator.IQuestionEvaluator;
import com.cinx.learning.service.quiz.evaluator.QuestionEvaluatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizSyncConsumer {

    private final QuizScoreAggregator quizScoreAggregator;
    private final QuizSnapshotBuilder snapshotBuilder;
    private final QuestionEvaluatorFactory questionEvaluatorFactory;
    private final QuizSessionQuestionRepository quizSessionQuestionRepository;
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final QuizScoreHistoryRepository quizScoreHistoryRepository;
    private final ILearningProgressService learningProgressService;

    @RabbitListener(queues = "learning.sync-and-regrade.queue", containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void receiveQuizSyncEvent(QuizSyncEvent event) {
        log.info("Received QuizSyncEvent: quizLessonId={}, changedQuestions={}",
                event.getQuizLessonId(), event.getQuestions().size());

        Map<String, QuizQuestionResponse> incomingMap = event.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestionResponse::id, q -> q));

        List<String> affectedQuestionIds = new ArrayList<>(incomingMap.keySet());

        List<QuizSessionQuestion> affectedRows = quizSessionQuestionRepository
                .findAllByQuizLessonIdAndQuestionIdIn(event.getQuizLessonId(), affectedQuestionIds);

        if (affectedRows.isEmpty()) {
            log.info("No QuizSessionQuestion rows found for affected questions in quizLessonId={}",
                    event.getQuizLessonId());
            return;
        }

        List<QuizSessionQuestion> needRegrade = new ArrayList<>();

        for (QuizSessionQuestion row : affectedRows) {
            QuizQuestionResponse incoming = incomingMap.get(row.getQuestionId());
            if (incoming == null) continue;

            String newCorrectAnswer = snapshotBuilder.buildCorrectAnswer(incoming);
            String newOptionsSnapshot = snapshotBuilder.buildOptionsSnapshot(incoming, false);

            boolean scoringAffected = false;

            if (!Objects.equals(row.getCorrectAnswer(), newCorrectAnswer)) {
                row.setCorrectAnswer(newCorrectAnswer);
                scoringAffected = true;
            }
            if (incoming.scoringMethod() != null
                    && !Objects.equals(row.getScoringMethod(), incoming.scoringMethod())) {
                row.setScoringMethod(incoming.scoringMethod());
                scoringAffected = true;
            }

            row.setQuestionText(incoming.questionText());
            row.setOptionsSnapshot(newOptionsSnapshot);

            if (scoringAffected) {
                needRegrade.add(row);
            }
        }

        quizSessionQuestionRepository.saveAll(affectedRows);
        log.info("Updated {} QuizSessionQuestion rows; {} need regrade", affectedRows.size(), needRegrade.size());

        if (needRegrade.isEmpty()) {
            log.info("No scoring impact detected — snapshot updated, no regrade needed.");
            return;
        }

        Map<String, QuizSession> affectedSessions = needRegrade.stream()
                .map(QuizSessionQuestion::getQuizSession)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(QuizSession::getId, s -> s, (a, b) -> a));
        Map<String, QuizSessionSubmission> submissionMap = quizSessionSubmissionRepository
                .findAllByQuizSessionIdIn(affectedSessions.keySet())
                .stream()
                .collect(Collectors.toMap(QuizSessionSubmission::getQuizSessionId, s -> s));

        Map<String, List<QuizSessionQuestion>> questionsMap = quizSessionQuestionRepository
                .findAllByQuizSessionIdIn(affectedSessions.keySet())
                .stream()
                .collect(Collectors.groupingBy(QuizSessionQuestion::getQuizSessionId));

        int regradeCount = 0;

        for (QuizSession session : affectedSessions.values()) {
            QuizSessionStatus status = session.getStatus();

            if (status == QuizSessionStatus.IN_PROGRESS) {
                log.debug("Session {} is IN_PROGRESS — snapshot updated, will be graded on submit.", session.getId());
                continue;
            }

            List<QuizSessionQuestion> allSessionQuestions = questionsMap.getOrDefault(session.getId(), Collections.emptyList());

            double totalFraction = 0.0;
            int correctCount = 0;
            for (QuizSessionQuestion q : allSessionQuestions) {
                if (q.getQuestionType() == QuizQuestionType.ESSAY) {
                    // PENDING_GRADE: keep existing essay score, non-essay will be re-evaluated
                    totalFraction += q.getScore() != null ? q.getScore() : 0.0;
                    continue;
                }
                IQuestionEvaluator evaluator = questionEvaluatorFactory.resolve(q);
                double fraction = evaluator.evaluate(q);
                q.setScore(fraction);
                if (fraction > 0.0) correctCount++;
                totalFraction += fraction;
            }
            quizSessionQuestionRepository.saveAll(allSessionQuestions);

            double newRawScore = (totalFraction / allSessionQuestions.size()) * 10.0;

            QuizSessionSubmission submission = submissionMap.get(session.getId());

            double oldScore = submission.getScore() != null ? submission.getScore() : 0.0;
            submission.setScore(newRawScore);
            submission.setTotalCorrectAnswers(correctCount);
            quizSessionSubmissionRepository.save(submission);

            quizScoreHistoryRepository.save(QuizScoreHistory.builder()
                    .quizSessionId(session.getId())
                    .oldScore(oldScore)
                    .newScore(newRawScore)
                    .reason(event.getChangeReason())
                    .gradedAt(LocalDateTime.now())
                    .build());
            regradeCount++;
            log.info("Regraded session {} | old={} new={}", session.getId(), oldScore, newRawScore);
            updateLearningProgress(session.getUserId(), session.getQuizLessonId(), event.getScoringMode());
        }

        log.info("Regrade complete for quizLessonId={}. {} sessions had score changes.",
                event.getQuizLessonId(), regradeCount);
    }

    private void updateLearningProgress(String userId, String quizLessonId, ScoringMode scoringMode) {
        double effectiveScore = quizScoreAggregator.aggregateScore(userId, quizLessonId, scoringMode);

        learningProgressService.updateLearningItemProgress(
                userId,
                quizLessonId,
                new UpdateLearningItemRequest(true, effectiveScore >= 5.0, effectiveScore)
        );

        log.info("Updated learning progress for userId={} quizLessonId={} effectiveScore={}",
                userId, quizLessonId, effectiveScore);
    }
}
