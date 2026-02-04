package com.cinx.course.service.course;

import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseService {
    CourseResponse getCourseById(String courseId);
    Page<CourseResponse> getAllCourses(String query, Pageable pageable);
    CourseResponse createCourse(CreateCourseRequest course);
    Course updateCourse(String courseId, Course courseDetails);
    void deleteCourse(String courseId);
}
