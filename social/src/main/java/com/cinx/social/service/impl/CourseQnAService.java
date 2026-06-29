package com.cinx.social.service.impl;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.dto.ApiResponse;
import com.cinx.common.mapper.SortConverter;
import com.cinx.social.client.EnrollmentClient;
import com.cinx.social.dto.request.CreateAnswerRequest;
import com.cinx.social.dto.request.CreateQuestionRequest;
import com.cinx.social.dto.request.UpdateAnswerRequest;
import com.cinx.social.dto.request.UpdateQuestionRequest;
import com.cinx.social.dto.response.AnswerDto;
import com.cinx.social.dto.response.CheckEnrollmentStatus;
import com.cinx.social.dto.response.CourseResponse;
import com.cinx.social.dto.response.QuestionDto;
import com.cinx.social.event.CourseAnswerCreatedEvent;
import com.cinx.social.event.CourseQuestionCreatedEvent;
import com.cinx.social.mapper.CourseQnAMapper;
import com.cinx.social.messaging.CourseQnAEventPublisher;
import com.cinx.social.model.AnswerUpvote;
import com.cinx.social.model.CourseAnswer;
import com.cinx.social.model.CourseQuestion;
import com.cinx.social.model.QuestionUpvote;
import com.cinx.social.repository.AnswerUpvoteRepository;
import com.cinx.social.repository.CourseAnswerRepository;
import com.cinx.social.repository.CourseQuestionRepository;
import com.cinx.social.repository.QuestionUpvoteRepository;
import com.cinx.social.service.ICourseQnAService;
import com.cinx.social.service.course.CourseService;
import com.cinx.social.model.Report;
import com.cinx.social.model.ReportType;
import com.cinx.social.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseQnAService implements ICourseQnAService {
    private final CourseQuestionRepository questionRepository;
    private final CourseAnswerRepository answerRepository;
    private final QuestionUpvoteRepository questionUpvoteRepository;
    private final AnswerUpvoteRepository answerUpvoteRepository;
    private final ReportRepository reportRepository;
    private final CourseQnAMapper mapper;
    private final EnrollmentClient enrollmentClient;
    private final CourseService courseService;
    private final CourseQnAEventPublisher eventPublisher;

    private void verifyEnrollment(String userId, String courseId) {
        ApiResponse<List<CheckEnrollmentStatus>> response = enrollmentClient.checkEnrollmentStatus(List.of(courseId));
        if (response == null || !response.success() || response.data().isEmpty() || !response.data().get(0).isEnrolled()) {
            throw new ForbiddenException(ErrorCode.NOT_ENROLLED_IN_COURSE, "Not enrolled in this course");
        }
    }

    private boolean isCourseInstructor(String userId, String courseId) {
        ApiResponse<CourseResponse> response = courseService.getCourseById(courseId);
        if (response == null || !response.success() || response.data() == null) {
            throw new NotFoundException("Course not found");
        }
        CourseResponse course = response.data();
        return course.instructor() != null && userId.equals(course.instructor().id());
    }

    @Override
    @Transactional
    public QuestionDto createQuestion(String userId, CreateQuestionRequest request) {
        verifyEnrollment(userId, request.getCourseId());

        CourseQuestion question = mapper.toModel(request);
        question.setUserId(userId);
        question.setUpvoteCount(defaultCount(question.getUpvoteCount()));
        question = questionRepository.save(question);

        CourseQuestionCreatedEvent event = CourseQuestionCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .questionId(question.getId())
                .courseId(question.getCourseId())
                .lessonId(question.getLessonId())
                .askedByUserId(userId)
                .questionTitle(question.getTitle())
                .occurredAt(Instant.now())
                .build();
        eventPublisher.publishQuestionCreatedEvent(event);

        return mapper.toDto(question);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionDto> getQuestionsByCourse(String courseId, String lessonId, String currentUserId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        Page<CourseQuestion> courseQuestionPage;
        if (lessonId != null && !lessonId.isEmpty()) {
            courseQuestionPage = questionRepository.findByCourseIdAndLessonId(courseId, lessonId, pageable);
        } else {
            courseQuestionPage = questionRepository.findByCourseId(courseId, pageable);
        }
        return courseQuestionPage.map(q -> {
            QuestionDto dto = mapper.toDto(q);
            if (currentUserId != null) {
                dto.setHasUpvoted(questionUpvoteRepository.existsByQuestionIdAndUserId(q.getId(), currentUserId));
            }
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionDto getQuestionById(String questionId, String currentUserId) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        
        QuestionDto dto = mapper.toDto(question);
        if (currentUserId != null) {
            dto.setHasUpvoted(questionUpvoteRepository.existsByQuestionIdAndUserId(questionId, currentUserId));
        }

        dto.setAnswersCount(answerRepository.countByQuestionId(questionId));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerDto> getAnswersForQuestion(String questionId, String currentUserId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        Page<CourseAnswer> answers = answerRepository.findByQuestionIdAndParentAnswerIdIsNullOrderByCreatedAtAsc(questionId, pageable);
        return answers.map(a -> mapAnswerToDtoWithRepliesCount(a, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerDto> getReplies(String parentAnswerId, String currentUserId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        Page<CourseAnswer> replies = answerRepository.findByParentAnswerIdOrderByCreatedAtAsc(parentAnswerId, pageable);
        return replies.map(r -> mapAnswerToDtoWithRepliesCount(r, currentUserId));
    }

    private AnswerDto mapAnswerToDtoWithRepliesCount(CourseAnswer answer, String currentUserId) {
        AnswerDto dto = mapper.toDto(answer);
        if (currentUserId != null) {
            dto.setHasUpvoted(answerUpvoteRepository.existsByAnswerIdAndUserId(answer.getId(), currentUserId));
        }
        dto.setRepliesCount(answerRepository.countByParentAnswerId(answer.getId()));
        return dto;
    }

    @Override
    @Transactional
    public QuestionDto updateQuestion(String userId, String questionId, UpdateQuestionRequest request) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        if (!question.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Not the owner");
        }
        mapper.partialUpdate(question, request);
        question = questionRepository.save(question);
        return mapper.toDto(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(String userId, String questionId) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        if (!question.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Not the owner");
        }
        questionRepository.delete(question);
    }

    @Override
    @Transactional
    public void upvoteQuestion(String userId, String questionId) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        
        verifyEnrollment(userId, question.getCourseId());

        if (questionUpvoteRepository.existsByQuestionIdAndUserId(questionId, userId)) {
            throw new AlreadyExistException(ErrorCode.ALREADY_UPVOTED, "Already upvoted");
        }

        QuestionUpvote upvote = QuestionUpvote.builder()
                .questionId(questionId)
                .userId(userId)
                .build();
        questionUpvoteRepository.save(upvote);
        
        question.setUpvoteCount(defaultCount(question.getUpvoteCount()) + 1);
        questionRepository.save(question);
    }

    @Override
    @Transactional
    public void reportQuestion(String userId, String questionId, com.cinx.social.dto.request.CreateQnAReportRequest request) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));
        Report report = Report.builder()
                .refId(questionId)
                .type(ReportType.QUESTION)
                .reporterId(userId)
                .reason(request.getReason())
                .build();
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public AnswerDto createAnswer(String userId, String questionId, CreateAnswerRequest request) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        boolean instructorAnswer = isCourseInstructor(userId, question.getCourseId());
        if (!instructorAnswer) {
            verifyEnrollment(userId, question.getCourseId());
        }

        CourseAnswer answer = mapper.toModel(request);
        answer.setUserId(userId);
        answer.setQuestionId(questionId);
        answer.setUpvoteCount(defaultCount(answer.getUpvoteCount()));
        answer.setIsInstructorAnswer(instructorAnswer);
        
        String parentAuthorId = null;
        if (request.getParentAnswerId() != null) {
            CourseAnswer parent = answerRepository.findById(request.getParentAnswerId())
                    .orElseThrow(() -> new NotFoundException("Parent answer not found"));
            answer.setDepth(defaultCount(parent.getDepth()) + 1);
            parentAuthorId = parent.getUserId();
        } else {
            answer.setDepth(0);
        }

        answer = answerRepository.save(answer);

        CourseAnswerCreatedEvent event = CourseAnswerCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .answerId(answer.getId())
                .questionId(question.getId())
                .courseId(question.getCourseId())
                .questionAuthorId(question.getUserId())
                .parentAnswerAuthorId(parentAuthorId)
                .answeredByUserId(userId)
                .occurredAt(Instant.now())
                .build();
        eventPublisher.publishAnswerCreatedEvent(event);

        return mapper.toDto(answer);
    }



    @Override
    @Transactional
    public AnswerDto updateAnswer(String userId, String answerId, UpdateAnswerRequest request) {
        CourseAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));
        if (!answer.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Not the owner");
        }
        mapper.partialUpdate(answer, request);
        answer = answerRepository.save(answer);
        return mapper.toDto(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(String userId, String answerId) {
        CourseAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));
        if (!answer.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Not the owner");
        }
        answerRepository.delete(answer);
    }

    @Override
    @Transactional
    public void upvoteAnswer(String userId, String answerId) {
        CourseAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));
                
        CourseQuestion question = questionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new NotFoundException("Question not found"));
        
        verifyEnrollment(userId, question.getCourseId());

        if (answerUpvoteRepository.existsByAnswerIdAndUserId(answerId, userId)) {
            throw new AlreadyExistException(ErrorCode.ALREADY_UPVOTED, "Already upvoted");
        }

        AnswerUpvote upvote = AnswerUpvote.builder()
                .answerId(answerId)
                .userId(userId)
                .build();
        answerUpvoteRepository.save(upvote);

        answer.setUpvoteCount(defaultCount(answer.getUpvoteCount()) + 1);
        answerRepository.save(answer);
    }

    @Override
    @Transactional
    public void reportAnswer(String userId, String answerId, com.cinx.social.dto.request.CreateQnAReportRequest request) {
        CourseAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));
        Report report = Report.builder()
                .refId(answerId)
                .type(ReportType.ANSWER)
                .reporterId(userId)
                .reason(request.getReason())
                .build();
        reportRepository.save(report);
    }

    private int defaultCount(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean defaultBoolean(Boolean value) {
        return value != null && value;
    }
}
