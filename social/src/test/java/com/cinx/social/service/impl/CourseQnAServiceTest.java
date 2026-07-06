package com.cinx.social.service.impl;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.social.client.EnrollmentClient;
import com.cinx.social.dto.request.CreateAnswerRequest;
import com.cinx.social.dto.request.CreateQuestionRequest;
import com.cinx.social.dto.response.AnswerDto;
import com.cinx.social.dto.response.CheckEnrollmentStatus;
import com.cinx.social.dto.response.CourseResponse;
import com.cinx.social.dto.response.InstructorResponse;
import com.cinx.social.dto.response.QuestionDto;
import com.cinx.social.mapper.CourseQnAMapper;
import com.cinx.social.messaging.CourseQnAEventPublisher;
import com.cinx.social.model.CourseAnswer;
import com.cinx.social.model.CourseQuestion;
import com.cinx.social.model.ReportType;
import com.cinx.social.model.AnswerUpvote;
import com.cinx.social.model.QuestionUpvote;
import com.cinx.social.repository.AnswerUpvoteRepository;
import com.cinx.social.repository.CourseAnswerRepository;
import com.cinx.social.repository.CourseQuestionRepository;
import com.cinx.social.repository.QuestionUpvoteRepository;
import com.cinx.social.repository.ReportRepository;
import com.cinx.social.service.course.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseQnAServiceTest {
    @Mock
    private CourseQuestionRepository questionRepository;
    @Mock
    private CourseAnswerRepository answerRepository;
    @Mock
    private QuestionUpvoteRepository questionUpvoteRepository;
    @Mock
    private AnswerUpvoteRepository answerUpvoteRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private CourseQnAMapper mapper;
    @Mock
    private EnrollmentClient enrollmentClient;
    @Mock
    private CourseService courseService;
    @Mock
    private CourseQnAEventPublisher eventPublisher;

    @InjectMocks
    private CourseQnAService courseQnAService;

    @Test
    void getQuestionsByCourseSetsAnswersCount() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .lessonId("lesson-1")
                .userId("user-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");

        QuestionDto mapped = new QuestionDto();
        mapped.setId("question-1");

        when(questionRepository.findByCourseId(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(question)));
        when(mapper.toDto(question)).thenReturn(mapped);
        when(questionUpvoteRepository.existsByQuestionIdAndUserId("question-1", "user-2")).thenReturn(true);
        when(answerRepository.countByQuestionId("question-1")).thenReturn(3);

        Page<QuestionDto> result = courseQnAService.getQuestionsByCourse("course-1", null, "user-2", 1, 10, "");

        QuestionDto dto = result.getContent().get(0);
        assertThat(dto.getAnswersCount()).isEqualTo(3);
        assertThat(dto.getHasUpvoted()).isTrue();
    }

    @Test
    void createQuestionDefaultsUpvoteCountBeforeSaving() {
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setCourseId("course-1");
        request.setLessonId("lesson-1");
        request.setTitle("Question title");
        request.setContent("Question content");

        CourseQuestion mapped = CourseQuestion.builder()
                .courseId("course-1")
                .lessonId("lesson-1")
                .title("Question title")
                .content("Question content")
                .build();
        QuestionDto response = new QuestionDto();
        response.setId("question-1");
        response.setUpvoteCount(0);

        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(mapper.toModel(request)).thenReturn(mapped);
        when(questionRepository.save(any(CourseQuestion.class))).thenAnswer(invocation -> {
            CourseQuestion question = invocation.getArgument(0);
            question.setId("question-1");
            return question;
        });
        when(mapper.toDto(any(CourseQuestion.class))).thenReturn(response);

        QuestionDto result = courseQnAService.createQuestion("user-1", request);

        assertThat(result.getUpvoteCount()).isEqualTo(0);
        assertThat(result.getAnswersCount()).isZero();
        verify(questionRepository).save(argThat(question ->
                "user-1".equals(question.getUserId())
                        && Integer.valueOf(0).equals(question.getUpvoteCount())));
    }

    @Test
    void createAnswerAllowsCourseInstructorWithoutEnrollment() {
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setQuestionId("question-1");
        request.setContent("Instructor answer");

        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("student-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");

        CourseAnswer mapped = CourseAnswer.builder()
                .content("Instructor answer")
                .build();

        AnswerDto response = new AnswerDto();
        response.setId("answer-1");
        response.setIsInstructorAnswer(true);

        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));
        when(courseService.getCourseById("course-1"))
                .thenReturn(new ApiResponse<>(true, "ok", new CourseResponse(
                        "course-1",
                        "Course title",
                        "Course description",
                        null,
                        new InstructorResponse("instructor-1", "Instructor", "avatar_url"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )));
        when(mapper.toModel(request)).thenReturn(mapped);
        when(answerRepository.save(any(CourseAnswer.class))).thenAnswer(invocation -> {
            CourseAnswer answer = invocation.getArgument(0);
            answer.setId("answer-1");
            return answer;
        });
        when(mapper.toDto(any(CourseAnswer.class))).thenReturn(response);

        AnswerDto result = courseQnAService.createAnswer("instructor-1", "question-1", request);

        assertThat(result.getIsInstructorAnswer()).isTrue();
        assertThat(result.getRepliesCount()).isZero();
        verify(enrollmentClient, never()).checkEnrollmentStatus(any());
        verify(answerRepository).save(argThat(answer ->
                "instructor-1".equals(answer.getUserId())
                        && "question-1".equals(answer.getQuestionId())
                        && Boolean.TRUE.equals(answer.getIsInstructorAnswer())));
    }

    @Test
    void deleteQuestionCleansAnswersUpvotesReportsAndQuestion() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("owner-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");

        CourseAnswer answer = answer("answer-1", "question-1", null, "owner-2");
        CourseAnswer reply = answer("answer-2", "question-1", "answer-1", "owner-3");

        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionId("question-1")).thenReturn(List.of(answer, reply));

        courseQnAService.deleteQuestion("owner-1", "question-1");

        verify(answerUpvoteRepository).deleteByAnswerIdIn(List.of("answer-1", "answer-2"));
        verify(reportRepository).deleteByRefIdAndType("answer-1", ReportType.ANSWER);
        verify(reportRepository).deleteByRefIdAndType("answer-2", ReportType.ANSWER);
        verify(answerRepository).deleteByIdIn(List.of("answer-1", "answer-2"));
        verify(questionUpvoteRepository).deleteByQuestionId("question-1");
        verify(reportRepository).deleteByRefIdAndType("question-1", ReportType.QUESTION);
        verify(questionRepository).delete(question);
    }

    @Test
    void deleteQuestionKeepsOwnerCheck() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("owner-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");

        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> courseQnAService.deleteQuestion("other-user", "question-1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Not the owner");
    }

    @Test
    void deleteAnswerCleansNestedRepliesUpvotesReportsAndAnswers() {
        CourseAnswer answer = answer("answer-1", "question-1", null, "owner-1");
        CourseAnswer reply = answer("answer-2", "question-1", "answer-1", "owner-2");
        CourseAnswer nestedReply = answer("answer-3", "question-1", "answer-2", "owner-3");

        when(answerRepository.findById("answer-1")).thenReturn(Optional.of(answer));
        when(answerRepository.findByParentAnswerId("answer-1")).thenReturn(List.of(reply));
        when(answerRepository.findByParentAnswerId("answer-2")).thenReturn(List.of(nestedReply));
        when(answerRepository.findByParentAnswerId("answer-3")).thenReturn(List.of());

        courseQnAService.deleteAnswer("owner-1", "answer-1");

        verify(answerUpvoteRepository).deleteByAnswerIdIn(List.of("answer-1", "answer-2", "answer-3"));
        verify(reportRepository).deleteByRefIdAndType("answer-1", ReportType.ANSWER);
        verify(reportRepository).deleteByRefIdAndType("answer-2", ReportType.ANSWER);
        verify(reportRepository).deleteByRefIdAndType("answer-3", ReportType.ANSWER);
        verify(answerRepository).deleteByIdIn(List.of("answer-1", "answer-2", "answer-3"));
    }

    @Test
    void deleteAnswerKeepsOwnerCheck() {
        CourseAnswer answer = answer("answer-1", "question-1", null, "owner-1");
        when(answerRepository.findById("answer-1")).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> courseQnAService.deleteAnswer("other-user", "answer-1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Not the owner");
    }

    @Test
    void deleteAnswerByAdminBypassesOwnerCheckAndUsesSameCleanup() {
        CourseAnswer answer = answer("answer-1", "question-1", null, "owner-1");

        when(answerRepository.findById("answer-1")).thenReturn(Optional.of(answer));
        when(answerRepository.findByParentAnswerId("answer-1")).thenReturn(List.of());

        courseQnAService.deleteAnswerByAdmin("answer-1");

        verify(answerUpvoteRepository).deleteByAnswerIdIn(List.of("answer-1"));
        verify(reportRepository).deleteByRefIdAndType("answer-1", ReportType.ANSWER);
        verify(answerRepository).deleteByIdIn(List.of("answer-1"));
    }

    @Test
    void upvoteQuestionCreatesUpvoteWhenNotAlreadyUpvoted() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("owner-1")
                .title("Question title")
                .content("Question content")
                .upvoteCount(2)
                .build();
        question.setId("question-1");

        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));
        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(questionUpvoteRepository.findByQuestionIdAndUserId("question-1", "user-1")).thenReturn(Optional.empty());

        courseQnAService.upvoteQuestion("user-1", "question-1");

        verify(questionUpvoteRepository).save(argThat(upvote ->
                "question-1".equals(upvote.getQuestionId()) && "user-1".equals(upvote.getUserId())));
        verify(questionRepository).save(argThat(savedQuestion -> savedQuestion.getUpvoteCount() == 3));
    }

    @Test
    void upvoteQuestionRemovesUpvoteWhenAlreadyUpvoted() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("owner-1")
                .title("Question title")
                .content("Question content")
                .upvoteCount(2)
                .build();
        question.setId("question-1");
        QuestionUpvote existingUpvote = QuestionUpvote.builder()
                .questionId("question-1")
                .userId("user-1")
                .build();

        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));
        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(questionUpvoteRepository.findByQuestionIdAndUserId("question-1", "user-1")).thenReturn(Optional.of(existingUpvote));

        courseQnAService.upvoteQuestion("user-1", "question-1");

        verify(questionUpvoteRepository).delete(existingUpvote);
        verify(questionUpvoteRepository, never()).save(any());
        verify(questionRepository).save(argThat(savedQuestion -> savedQuestion.getUpvoteCount() == 1));
    }

    @Test
    void upvoteAnswerCreatesUpvoteWhenNotAlreadyUpvoted() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("owner-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");
        CourseAnswer answer = answer("answer-1", "question-1", null, "owner-2");
        answer.setUpvoteCount(2);

        when(answerRepository.findById("answer-1")).thenReturn(Optional.of(answer));
        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));
        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(answerUpvoteRepository.findByAnswerIdAndUserId("answer-1", "user-1")).thenReturn(Optional.empty());

        courseQnAService.upvoteAnswer("user-1", "answer-1");

        verify(answerUpvoteRepository).save(argThat(upvote ->
                "answer-1".equals(upvote.getAnswerId()) && "user-1".equals(upvote.getUserId())));
        verify(answerRepository).save(argThat(savedAnswer -> savedAnswer.getUpvoteCount() == 3));
    }

    @Test
    void upvoteAnswerRemovesUpvoteWhenAlreadyUpvoted() {
        CourseQuestion question = CourseQuestion.builder()
                .courseId("course-1")
                .userId("owner-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");
        CourseAnswer answer = answer("answer-1", "question-1", null, "owner-2");
        answer.setUpvoteCount(2);
        AnswerUpvote existingUpvote = AnswerUpvote.builder()
                .answerId("answer-1")
                .userId("user-1")
                .build();

        when(answerRepository.findById("answer-1")).thenReturn(Optional.of(answer));
        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question));
        when(enrollmentClient.checkEnrollmentStatus(List.of("course-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(new CheckEnrollmentStatus("course-1", true))));
        when(answerUpvoteRepository.findByAnswerIdAndUserId("answer-1", "user-1")).thenReturn(Optional.of(existingUpvote));

        courseQnAService.upvoteAnswer("user-1", "answer-1");

        verify(answerUpvoteRepository).delete(existingUpvote);
        verify(answerUpvoteRepository, never()).save(any());
        verify(answerRepository).save(argThat(savedAnswer -> savedAnswer.getUpvoteCount() == 1));
    }

    private CourseAnswer answer(String id, String questionId, String parentAnswerId, String userId) {
        CourseAnswer answer = CourseAnswer.builder()
                .questionId(questionId)
                .parentAnswerId(parentAnswerId)
                .userId(userId)
                .content("Answer content")
                .build();
        answer.setId(id);
        return answer;
    }
}
