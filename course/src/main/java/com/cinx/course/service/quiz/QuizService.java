package com.cinx.course.service.quiz;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.*;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.dto.response.QuizQuestionResponse;
import com.cinx.course.mapper.QuizMapper;
import com.cinx.course.mapper.QuizQuestionMapper;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.QuizSyncEvent;
import com.cinx.course.messaging.event.ScoringModeChangedEvent;
import com.cinx.course.model.QuizLesson;
import com.cinx.course.model.QuizQuestion;
import com.cinx.course.repository.QuizLessonRepository;
import com.cinx.course.repository.QuizQuestionRepository;
import com.cinx.course.service.lesson.ILessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {
    private final QuizLessonRepository quizLessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final IQuizQuestionService quizQuestionService;
    private final ILessonService lessonService;
    private final QuizMapper quizMapper;
    private final QuizQuestionMapper quizQuestionMapper;
    private final CourseEventProducer courseEventProducer;

    @Override
    @Transactional(readOnly = true)
    public QuizLessonResponse getQuizByLessonId(String lessonId) {
        return quizLessonRepository.findByLessonId(lessonId)
                .map(quizMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));
    }

    @Transactional
    @Override
    public void createQuiz(String lessonId, CreateQuizLessonRequest request) {
        if (request.getQuestions().size() < request.getNumberOfQuestionPerQuizSession()) {
            throw new BadRequestException(
                    "Number of questions must be >= numberOfQuestionPerQuizSession");
        }

        quizLessonRepository.findByLessonId(lessonId).ifPresent(existing -> {
            throw new AlreadyExistException("Quiz already exists for lessonId: " + lessonId);
        });

        QuizLesson quizLesson = quizMapper.toModel(request);
        quizLesson.setLessonId(lessonId);
        QuizLesson saved = quizLessonRepository.save(quizLesson);

        quizQuestionService.addQuestions(saved.getLessonId(), request.getQuestions());
    }

    @Transactional
    @Override
    public void updateQuiz(String lessonId, UpdateQuizLessonRequest request) {
        QuizLesson existing = getOrThrow(lessonId);

        boolean scoringModeChanged = request.getScoringMode() != null
                && !Objects.equals(existing.getScoringMode(), request.getScoringMode());

        if (request.getNumberOfQuestionPerQuizSession() != null) {
            int questionCount = quizQuestionRepository.countByQuizLessonId(existing.getLessonId());
            if (request.getNumberOfQuestionPerQuizSession() > questionCount) {
                throw new BadRequestException(
                        "numberOfQuestionPerQuizSession (" + request.getNumberOfQuestionPerQuizSession()
                                + ") cannot exceed current question count (" + questionCount + ")");
            }
        }

        quizMapper.partialUpdate(existing, request);

        if (scoringModeChanged) {
            courseEventProducer.publishScoringModeChangedEvent(
                    new ScoringModeChangedEvent(existing.getLessonId(), existing.getScoringMode()));
        }

        quizLessonRepository.save(existing);
    }

    @Transactional
    @Override
    public void syncQuiz(String lessonId, SyncQuizRequest request) {
        QuizLesson existing = getOrThrow(lessonId);

        if (Boolean.TRUE.equals(request.triggerRegrade())) {
            List<QuizQuestion> questions = quizQuestionRepository.findAllByQuizLessonIdAndNeedSync(lessonId);

            List<QuizQuestionResponse> snapshots = questions.stream()
                    .map(quizQuestionMapper::toDto)
                    .toList();

            QuizSyncEvent syncEvent = QuizSyncEvent.builder()
                    .quizLessonId(existing.getLessonId())
                    .scoringMode(existing.getScoringMode())
                    .changeReason(request.changeReason())
                    .questions(snapshots)
                    .build();

            courseEventProducer.publishQuizSyncEvent(syncEvent);
        }

        existing.setHasPendingSync(false);
        quizLessonRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizLesson getOrThrow(String lessonId) {
        return quizLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));
    }
}
