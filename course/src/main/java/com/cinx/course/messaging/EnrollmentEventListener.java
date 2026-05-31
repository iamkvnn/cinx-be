package com.cinx.course.messaging;

import com.cinx.course.messaging.event.EnrolledCourseEvent;
import com.cinx.course.model.CourseEnrollmentEvent;
import com.cinx.course.repository.CourseEnrollmentEventRepository;
import com.cinx.course.service.course.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentEventListener {
    private final CourseEnrollmentEventRepository eventRepository;
    private final ICourseService courseService;

    @Transactional
    @RabbitListener(queues = "course.enrollment.queue")
    public void onEnrollmentCreated(EnrolledCourseEvent event) {
        String id = event.getUserId() + ":" + event.getCourseId();
        if (eventRepository.existsById(id)) {
            log.info("Enrollment event already applied: {}", id);
            return;
        }
        eventRepository.save(CourseEnrollmentEvent.builder()
                .id(id)
                .userId(event.getUserId())
                .courseId(event.getCourseId())
                .build());
        courseService.increaseEnrollmentCount(event.getCourseId());
    }
}
