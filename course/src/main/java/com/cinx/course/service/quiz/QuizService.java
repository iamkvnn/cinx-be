package com.cinx.course.service.quiz;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateQuizLessonRequest;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.mapper.QuizMapper;
import com.cinx.course.model.QuizLesson;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.QuizLessonRepository;
import com.cinx.course.repository.QuizOptionRepository;
import com.cinx.course.repository.QuizQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService implements IQuizService {
    private final QuizLessonRepository quizLessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final LessonRepository lessonRepository;
    private final QuizMapper quizMapper;

    @Override
    public QuizLessonResponse getQuizByLessonId(String lessonId) {
        return quizLessonRepository.findByLessonId(lessonId)
                .map(quizMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));
    }

    @Transactional
    @Override
    public void createQuiz(String lessonId, CreateQuizLessonRequest request) {
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new BadRequestException("Quiz must contain at least one question");
        }
        if (request.getQuestions().size() < request.getNumberOfQuestionPerQuizSession()) {
            throw new BadRequestException("Number of questions must be greater than or equal to numberOfQuestionPerQuizSession");
        }
        quizLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            throw new AlreadyExistException("Quiz already exists for lessonId: " + lessonId);
        },() -> {
            QuizLesson quizLesson = quizMapper.toModel(request);
            quizLesson.setLesson(lessonRepository.findById(lessonId).orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId)));
            QuizLesson saved = quizLessonRepository.save(quizLesson);
            saved.setQuestions(quizQuestionRepository.saveAll(saved.getQuestions().stream()
                    .peek(q -> q.setQuizLesson(saved))
                    .toList()));
            quizOptionRepository.saveAll(saved.getQuestions().stream()
                    .flatMap(q -> q.getOptions().stream()
                            .peek(o -> o.setQuestionId(q.getId())))
                    .toList());
        });
    }

    @Transactional
    @Override
    public void updateQuiz(String lessonId, CreateQuizLessonRequest request) {
        quizLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            quizMapper.partialUpdate(existing, request);
            quizLessonRepository.save(existing);
            quizOptionRepository.deleteAll(existing.getQuestions().stream()
                    .flatMap(q -> q.getOptions().stream())
                    .toList());
            quizQuestionRepository.deleteAll(existing.getQuestions());
            quizQuestionRepository.saveAll(existing.getQuestions().stream()
                    .peek(q -> q.setQuizLesson(existing))
                    .toList());
            quizOptionRepository.saveAll(existing.getQuestions().stream()
                    .flatMap(q -> q.getOptions().stream()
                            .peek(o -> o.setQuizQuestion(q)))
                    .toList());
        },() -> {
            throw new NotFoundException("Quiz not found for lessonId: " + lessonId);
        });
    }

    @Override
    public void deleteQuiz(String lessonId) {
        quizLessonRepository.findByLessonId(lessonId)
                .ifPresentOrElse(quizLessonRepository::delete, () -> {
                    throw new NotFoundException("Quiz not found for lessonId: " + lessonId);
        });
    }
}
