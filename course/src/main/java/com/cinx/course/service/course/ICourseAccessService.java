package com.cinx.course.service.course;

import com.cinx.course.model.Course;

import java.util.List;
import java.util.Map;

public interface ICourseAccessService {
    Course ensureReadableCourse(String currentUserId, String courseId);
    boolean canReadCourse(String currentUserId, Course course);
    boolean canReadCourse(String currentUserId, Course course, Map<String, Boolean> enrollmentByCourseId);
    Map<String, Boolean> enrollmentByCourseId(String currentUserId, List<Course> courses);
    boolean isAdmin();
    boolean isCourseOwner(String currentUserId, Course course);
    boolean isEnrolled(String currentUserId, String courseId);
    void ensureCurrentUserOwns(String currentUserId, Course course);
}
