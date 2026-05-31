package com.cinx.course.service.course;

import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.messaging.event.LessonChangedEvent;
import com.cinx.course.model.Category;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;

import java.util.List;
import java.util.Optional;

public interface ICourseDraftService {
    Optional<CourseDraft> findDraft(Course course);
    CourseDraft getOrCreateDraft(Course course);
    CourseDraft updateDraft(Course course, UpdateCourseRequest request, Category category, Long discountRate);
    List<LessonChangedEvent> approveDraft(Course course);
}
