package com.cinx.learning.messaging;

import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.messaging.event.ScoringModeChangedEvent;
import com.cinx.learning.model.QuizSessionSubmission;
import com.cinx.learning.repository.QuizSessionSubmissionRepository;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.quiz.QuizScoreAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizScoringModeChangedListener {
    private final QuizSessionSubmissionRepository quizSessionSubmissionRepository;
    private final ILearningProgressService learningProgressService;
    private final QuizScoreAggregator quizScoreAggregator;

    @Transactional
    @RabbitListener(queues = "learning.quiz.scoring-mode-change.queue", containerFactory = "rabbitListenerContainerFactory")
    public void onScoringModeChanged(ScoringModeChangedEvent event) {
        log.info("Received ScoringModeChangedEvent: quizLessonId={} newScoringMode={}",
                event.quizLessonId(), event.scoringMode());
        Map<String, List<QuizSessionSubmission>> submissionsByUser = quizSessionSubmissionRepository
                .findAllByQuizLessonId(event.quizLessonId())
                .stream()
                .collect(Collectors.groupingBy(s -> s.getQuizSession().getUserId()));


        for (Map.Entry<String, List<QuizSessionSubmission>> entry : submissionsByUser.entrySet()) {
            String userId = entry.getKey();
            List<QuizSessionSubmission> submissions = entry.getValue();

            List<Double> scores = submissions.stream()
                    .map(QuizSessionSubmission::getScore)
                    .toList();

            double newAggregatedScore = quizScoreAggregator.aggregateScore(scores, event.scoringMode());

            learningProgressService.updateLearningItemProgress(
                    userId,
                     event.quizLessonId(),
                    new UpdateLearningItemRequest(true, newAggregatedScore >= 5.0, newAggregatedScore)
            );
        }
    }
}
