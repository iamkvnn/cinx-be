package com.cinx.social.service.impl;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.client.EnrollmentClient;
import com.cinx.social.dto.request.CreateQuestionRequest;
import com.cinx.social.dto.response.CheckEnrollmentStatus;
import com.cinx.social.dto.response.QuestionDto;
import com.cinx.social.mapper.CourseQnAMapper;
import com.cinx.social.messaging.CourseQnAEventPublisher;
import com.cinx.social.model.CourseQuestion;
import com.cinx.social.repository.AnswerUpvoteRepository;
import com.cinx.social.repository.CourseAnswerRepository;
import com.cinx.social.repository.CourseQuestionRepository;
import com.cinx.social.repository.QuestionUpvoteRepository;
import com.cinx.social.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    private CourseQnAEventPublisher eventPublisher;

    @InjectMocks
    private CourseQnAService courseQnAService;

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
        verify(questionRepository).save(argThat(question ->
                "user-1".equals(question.getUserId())
                        && Integer.valueOf(0).equals(question.getUpvoteCount())));
    }
}
