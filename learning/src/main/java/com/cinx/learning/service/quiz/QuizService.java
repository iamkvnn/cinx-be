package com.cinx.learning.service.quiz;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.consts.QuizSessionStatus;
import com.cinx.learning.dto.request.ChooseQuizAnswerRequest;
import com.cinx.learning.dto.request.SubmitQuizSessionRequest;
import com.cinx.learning.dto.request.UpdateCourseProgressRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    if (quizSession.getStatus() == QuizSessionStatus.IN_PROGRESS) {
                        return new QuizSessionQuestionResponse(
                                dto.id(),
                                dto.quizSessionId(),
                                dto.questionId(),
                                dto.questionType(),
                                dto.questionOrder(),
                                dto.userAnswer(),
                                null,
                                null,
                                null
                        );
                    } else if (Boolean.FALSE.equals(quizSession.getIsShowAnswersOnReview())) {
                        return new QuizSessionQuestionResponse(
                                dto.id(),
                                dto.quizSessionId(),
                                dto.questionId(),
                                dto.questionType(),
                                dto.questionOrder(),
                                dto.userAnswer(),
                                null,
                                null,
                                null
                        );
                    }
                    return dto;
                });
    }

    @Transactional
    @Override
    public QuizSessionResponse createQuizSession(String userId, String quizLessonId) {
        QuizLessonResponse quizLessonResponse = courseService.getQuizLessonById(quizLessonId).data();
        if (quizLessonResponse.maxAttempt() <= quizSessionRepository.countByQuizLessonId(quizLessonId)) {
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
        createQuizSessionQuestions(quizSession, quizLessonResponse.numberOfQuestionPerQuizSession(), quizLessonResponse.questions());
        return quizSessionMapper.toDto(quizSession);
    }

    private void createQuizSessionQuestions(QuizSession quizSession, Integer numberOfQuestionPerQuizSession, List<QuizQuestionResponse> questions) {
        List<QuizSessionQuestion> quizSessionQuestions = new ArrayList<>();
        boolean[] usedIndexes = new boolean[questions.size()];
        for (int i = 0; i < numberOfQuestionPerQuizSession; i++) {
            int randomIndex;
            do {
                randomIndex = (int) (Math.random() * questions.size());
            } while (usedIndexes[randomIndex]);
            usedIndexes[randomIndex] = true;
            QuizQuestionResponse quizQuestionResponse = questions.get(randomIndex);
            quizSessionQuestions.add(QuizSessionQuestion.builder()
                    .quizSessionId(quizSession.getId())
                    .questionOrder(i + 1)
                    .questionType(quizQuestionResponse.questionType())
                    .questionId(quizQuestionResponse.id())
                    //.weight(quizQuestionResponse.weight())
                    .correctAnswer(quizQuestionResponse.options().stream().filter(QuizOptionResponse::isCorrect).map(QuizOptionResponse::optionOrder).sorted().toList().toString())
                    .build()
            );
        }
        quizSession.setQuestions(quizSessionQuestionRepository.saveAll(quizSessionQuestions));
    }

    @Override
    public void chooseQuizSessionQuestion(String quizSessionId, ChooseQuizAnswerRequest request) {
        quizSessionQuestionRepository.findByQuizSessionIdAndQuestionId(quizSessionId, request.questionId())
                .ifPresentOrElse(
                        quizSessionQuestion -> {
                            quizSessionQuestion.setUserAnswer(request.userAnswer());
                            quizSessionQuestionRepository.save(quizSessionQuestion);
                        },
                        () -> {
                            throw new NotFoundException("Quiz session question not found");
                        }
                );
    }

    @Transactional
    @Override
    public QuizSessionResponse submitQuizSession(String quizSessionId, SubmitQuizSessionRequest request) {
        QuizSession quizSession = quizSessionRepository.findById(quizSessionId)
                .orElseThrow(() -> new NotFoundException("Quiz session not found"));
        if (quizSession.getStatus() == QuizSessionStatus.SUBMITTED) {
            throw new BadRequestException("Quiz session already submitted");
        }
        if (quizSession.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Quiz session has expired");
        }
        quizSession.setStatus(QuizSessionStatus.SUBMITTED);
        quizSessionRepository.save(quizSession);
        Map<String, ChooseQuizAnswerRequest> quizAnswerRequestMap = request.answers().stream().collect(Collectors.toMap(ChooseQuizAnswerRequest::questionId, q -> q));

        Integer totalCorrectAnswers = quizSession.getQuestions().stream().peek(quizSessionQuestion -> {
            ChooseQuizAnswerRequest chooseQuizAnswerRequest = quizAnswerRequestMap.get(quizSessionQuestion.getQuestionId());
            if (chooseQuizAnswerRequest != null) {
                quizSessionQuestion.setUserAnswer(chooseQuizAnswerRequest.userAnswer());
            }
        }).reduce(0, (total, quizSessionQuestion) -> {
            if (quizSessionQuestion.getCorrectAnswer().equals(quizSessionQuestion.getUserAnswer())) {
                return total + 1;
            }
            return total;
        }, Integer::sum);

        quizSession.setQuestions(quizSessionQuestionRepository.saveAll(quizSession.getQuestions()));

        quizSession.setQuizSessionSubmission(quizSessionSubmissionRepository.save(
                QuizSessionSubmission.builder()
                        .quizSessionId(quizSession.getId())
                        .score((double) (totalCorrectAnswers * 10 / quizSession.getQuestions().size()))
                        .submissionTime(LocalDateTime.now())
                        .totalCorrectAnswers(totalCorrectAnswers)
                        .build()
        ));
        learningProgressService.updateLearningItemProgress(
                quizSession.getUserId(),
                quizSession.getQuizLessonId(),
                new UpdateLearningItemRequest(true, quizSession.getQuizSessionSubmission().getScore() >= 5.0, quizSession.getQuizSessionSubmission().getScore())
        );
        return quizSessionMapper.toDto(quizSession);
    }
}
