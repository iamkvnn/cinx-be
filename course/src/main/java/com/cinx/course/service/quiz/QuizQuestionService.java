package com.cinx.course.service.quiz;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;
import com.cinx.course.dto.request.CreateQuizOptionRequest;
import com.cinx.course.dto.request.CreateQuizQuestionRequest;
import com.cinx.course.dto.request.UpdateQuizOptionRequest;
import com.cinx.course.dto.request.UpdateQuizQuestionRequest;
import com.cinx.course.dto.response.QuizOptionResponse;
import com.cinx.course.dto.response.QuizQuestionResponse;
import com.cinx.course.mapper.QuizQuestionMapper;
import com.cinx.course.model.QuizLesson;
import com.cinx.course.model.QuizOption;
import com.cinx.course.model.QuizQuestion;
import com.cinx.course.repository.QuizLessonRepository;
import com.cinx.course.repository.QuizOptionRepository;
import com.cinx.course.repository.QuizQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizQuestionService implements IQuizQuestionService {
    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizLessonRepository quizLessonRepository;

    @Override
    @Transactional(readOnly = true)
    public List<QuizQuestionResponse> getQuestions(String lessonId) {
        quizLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));
        return quizQuestionRepository.findAllByQuizLessonId(lessonId).stream()
                .map(quizQuestionMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public QuizQuestionResponse addQuestion(String lessonId, CreateQuizQuestionRequest request) {
        QuizLesson quiz = quizLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));

        QuizQuestion savedQuestion = quizQuestionRepository.save(createQuestionEntity(quiz, request));
        savedQuestion.getOptions().forEach(o -> o.setQuizQuestion(savedQuestion));
        savedQuestion.setOptions(quizOptionRepository.saveAll(savedQuestion.getOptions()));

        return quizQuestionMapper.toDto(savedQuestion);
    }

    private QuizQuestion createQuestionEntity(QuizLesson lesson, CreateQuizQuestionRequest request) {
        return QuizQuestion.builder()
                .questionText(request.questionText())
                .questionType(request.questionType())
                .scoringMethod(request.scoringMethod())
                .quizLesson(lesson)
                .options(request.options()
                        .stream()
                        .<QuizOption>map(o -> QuizOption.builder()
                                .optionText(o.optionText())
                                .isCorrect(o.isCorrect())
                                .optionOrder(o.optionOrder())
                                .matchText(o.matchText())
                                .build()
                        ).toList()
                )
                .build();
    }

    @Transactional
    @Override
    public List<QuizQuestionResponse> addQuestions(String lessonId, List<CreateQuizQuestionRequest> requests) {
        QuizLesson quiz = quizLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));

        List<QuizQuestion> savedQuestions = quizQuestionRepository.saveAll(requests.stream()
                .map(req -> createQuestionEntity(quiz, req))
                .toList());

        List<QuizOption> allOptions = new ArrayList<>();
        savedQuestions.forEach(q ->
            q.getOptions().forEach(o -> {
                o.setQuizQuestion(q);
                allOptions.add(o);
            })
        );
        Map<String, List<QuizOption>> optionMap = quizOptionRepository.saveAll(allOptions).stream()
                .collect(Collectors.groupingBy(o -> o.getQuizQuestion().getId()));
        savedQuestions.forEach(q -> q.setOptions(optionMap.getOrDefault(q.getId(), Collections.emptyList())));

        return savedQuestions.stream().map(quizQuestionMapper::toDto).toList();
    }

    @Transactional
    @Override
    public QuizQuestionResponse updateQuestion(String lessonId, String questionId, UpdateQuizQuestionRequest request) {
        QuizLesson quiz = quizLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));
        QuizQuestion question = quizQuestionRepository.findByIdAndQuizLessonId(questionId, lessonId)
                .orElseThrow(() -> new NotFoundException("Question not found: " + questionId));

        boolean scoringChanged = isScoringChanged(question, request);

        question.setQuestionText(request.questionText());
        question.setScoringMethod(request.scoringMethod());
        question.setNeedSync(scoringChanged);

        quizQuestionRepository.save(question);
        mergeOptions(question, request.options());

        if (scoringChanged) {
            quiz.setHasPendingSync(true);
            quizLessonRepository.save(quiz);
        }

        return quizQuestionMapper.toDto(question);
    }

    @Transactional
    @Override
    public void deleteQuestion(String lessonId, String questionId) {
        QuizLesson quiz = quizLessonRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new NotFoundException("Quiz not found for lessonId: " + lessonId));
        QuizQuestion question = quizQuestionRepository.findByIdAndQuizLessonId(questionId, lessonId)
                .orElseThrow(() -> new NotFoundException("Question not found: " + questionId));

        int currentCount = quizQuestionRepository.countByQuizLessonId(lessonId);
        Integer minRequired = quiz.getNumberOfQuestionPerQuizSession();
        if (minRequired != null && currentCount <= minRequired) {
            throw new BadRequestException("Quiz must have at least " + minRequired + " question(s)");
        }

        quizOptionRepository.deleteAllByQuizQuestionId(questionId);
        quizQuestionRepository.delete(question);
    }

    private void mergeOptions(QuizQuestion question, List<UpdateQuizOptionRequest> incomingOptions) {
        List<QuizOption> existingOptions = question.getOptions();

        Map<String, QuizOption> existingById = existingOptions.stream()
                .collect(Collectors.toMap(QuizOption::getId, o -> o));
        Set<String> incomingIds = incomingOptions.stream()
                .map(UpdateQuizOptionRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        quizOptionRepository.deleteAllById(existingOptions.stream()
                .map(QuizOption::getId)
                .filter(id -> !incomingIds.contains(id))
                .toList());

        List<QuizOption> optionsToSave = new ArrayList<>();
        incomingOptions.forEach(o -> {
            QuizOption option;
            if (o.id() != null) {
                option = existingById.get(o.id());
                if (option == null) {
                    throw new NotFoundException("Option not found: " + o.id());
                }
            } else {
                option = new QuizOption();
                option.setQuizQuestion(question);
            }
            option.setOptionText(o.optionText());
            option.setIsCorrect(o.isCorrect());
            option.setOptionOrder(o.optionOrder());
            option.setMatchText(o.matchText());
            optionsToSave.add(option);
        });
        question.setOptions(quizOptionRepository.saveAll(optionsToSave));
    }

    private boolean isScoringChanged(QuizQuestion existing, UpdateQuizQuestionRequest request) {
        if (request.scoringMethod() != null
                && !Objects.equals(existing.getScoringMethod(), request.scoringMethod())) {
            return true;
        }

        if (request.options() != null) {
            String oldAnswer = buildCorrectAnswer(existing.getOptions(), existing.getQuestionType());

            List<QuizOption> newOptions = request.options().stream()
                    .<QuizOption>map(o -> QuizOption.builder()
                            .id(o.id())
                            .optionText(o.optionText())
                            .isCorrect(o.isCorrect())
                            .optionOrder(o.optionOrder())
                            .matchText(o.matchText())
                            .build())
                    .toList();
            String newAnswer = buildCorrectAnswer(newOptions, existing.getQuestionType());
            return !Objects.equals(oldAnswer, newAnswer);
        }
        return false;
    }

    @Override
    public String buildCorrectAnswer(List<QuizOption> options, QuizQuestionType questionType) {
        return switch (questionType) {
            case MATCHING -> options.stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .sorted(Comparator.comparing(o -> o.getOptionOrder() != null ? o.getOptionOrder() : 0))
                    .map(o -> (o.getId() != null ? o.getId() : "NEW") + ":" + (o.getMatchText() != null ? o.getMatchText() : ""))
                    .collect(Collectors.joining(","));
            case SHORT_TEXT, ESSAY -> options.stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(QuizOption::getOptionText)
                    .sorted()
                    .collect(Collectors.joining(","));
            default -> options.stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(o -> o.getId() != null ? o.getId() : "NEW")
                    .sorted()
                    .collect(Collectors.joining(","));
        };
    }
}
