package com.cinx.learning.service.certificate;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.consts.CertificateStatus;
import com.cinx.learning.mapper.CertificateRequestMapper;
import com.cinx.learning.messaging.NotificationPublisher;
import com.cinx.learning.repository.CertificateRequestRepository;
import com.cinx.learning.service.authorization.LearningAuthorizationService;
import com.cinx.learning.service.course.CourseService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import com.cinx.learning.service.s3.S3Service;
import com.cinx.learning.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {
    @Mock
    private CertificateRequestRepository certificateRequestRepository;
    @Mock
    private CertificateRequestMapper certificateRequestMapper;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;
    @Mock
    private ILearningProgressService learningProgressService;
    @Mock
    private CertificateGeneratorService certificateGeneratorService;
    @Mock
    private S3Service s3Service;
    @Mock
    private LearningAuthorizationService authorizationService;
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    @Test
    void getRequestsByCoursePassesQueryStatusAndSortToRepository() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(certificateRequestRepository.search(eq("course-1"), eq(CertificateStatus.PENDING), eq("user-1"), pageableCaptor.capture()))
                .thenReturn(Page.empty());

        certificateService.getRequestsByCourse(
                "instructor-1",
                "course-1",
                CertificateStatus.PENDING,
                1,
                10,
                " user-1 ",
                "{\"requestedAt\":\"DESC\"}");

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order order = pageable.getSort().getOrderFor("requestedAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        verify(authorizationService).requireCourseInstructor("instructor-1", "course-1");
    }

    @Test
    void getAllRequestsLimitsResultsToCurrentInstructorCourses() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        List<String> instructorCourseIds = List.of("course-1", "course-2");
        when(courseService.getCourseIdsByInstructor("instructor-1"))
                .thenReturn(new ApiResponse<>(true, "ok", instructorCourseIds));
        when(certificateRequestRepository.searchByCourseIds(
                eq(instructorCourseIds),
                eq(CertificateStatus.PENDING),
                eq("student-1"),
                pageableCaptor.capture()))
                .thenReturn(Page.empty());

        certificateService.getAllRequests(
                "instructor-1",
                CertificateStatus.PENDING,
                1,
                10,
                " student-1 ",
                "{\"requestedAt\":\"DESC\"}");

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order order = pageable.getSort().getOrderFor("requestedAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        verify(certificateRequestRepository, never()).search(eq(null), any(), any(), any());
    }

    @Test
    void getAllRequestsReturnsEmptyPageWhenInstructorHasNoCourses() {
        when(courseService.getCourseIdsByInstructor("instructor-1"))
                .thenReturn(new ApiResponse<>(true, "ok", List.of()));

        Page<?> result = certificateService.getAllRequests(
                "instructor-1",
                null,
                1,
                10,
                null,
                null);

        assertThat(result).isEmpty();
        verify(certificateRequestRepository, never()).searchByCourseIds(any(), any(), any(), any());
        verify(certificateRequestRepository, never()).search(eq(null), any(), any(), any());
    }
}
