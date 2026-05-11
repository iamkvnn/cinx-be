package com.cinx.enrollment.service.enrollment;

import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.dto.request.CreateEnrolledCourseRequest;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import com.cinx.enrollment.messaging.EnrolledCourseEventProducer;
import com.cinx.enrollment.messaging.event.EnrolledCourseEvent;
import com.cinx.enrollment.model.EnrolledCourse;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService implements IEnrollmentService {
    private final EnrolledCourseRepository enrolledCourseRepository;
    private final CourseService courseService;
    private final EnrolledCourseEventProducer enrolledCourseEventProducer;

    @Override
    public Page<CourseResponse> getEnrolledCourses(int page, int size) {
        String userId = AuthenticationUtil.extractUserId();
        Page<EnrolledCourse> enrolledCourses = enrolledCourseRepository.findAllByUserId(userId, PageRequest.of(page - 1, size));
        List<String> courseIds = enrolledCourses.getContent().stream()
            .map(EnrolledCourse::getCourseId)
            .toList();
        List<CourseResponse> courses = courseService.getCoursesByIds(courseIds).data();
        return new PageImpl<>(courses, PageRequest.of(page, size), courses.size());
    }

    @Override
    public List<CheckEnrollmentStatus> checkEnrollmentStatus(List<String> courseIds) {
        String userId = AuthenticationUtil.extractUserId();
        Map<String, EnrolledCourse> enrolledCourseMap = enrolledCourseRepository.findAllByUserIdAndCourseIdIn(userId, courseIds).stream()
            .collect(Collectors.toMap(EnrolledCourse::getCourseId, Function.identity()));
        return courseIds.stream()
            .map(courseId -> new CheckEnrollmentStatus(courseId, enrolledCourseMap.containsKey(courseId)))
            .toList();
    }

    @Override
    public void enrollCourses(List<CreateEnrolledCourseRequest> requests) {
        enrolledCourseRepository.saveAll(requests.stream()
            .map(req ->
                    EnrolledCourse.builder()
                            .courseId(req.courseId())
                            .userId(req.userId())
                            .build()
            ).toList()
        ).forEach(enrolledCourse ->
                enrolledCourseEventProducer.publishEnrolledCourseCreatedEvent(
                        new EnrolledCourseEvent(enrolledCourse.getCourseId(), enrolledCourse.getUserId())
                )
        );
    }

    @Override
    public List<String> getUserIdsEnrolledInCourse(String courseId) {
        return enrolledCourseRepository.findUserIdsByCourseId(courseId);
    }
}
