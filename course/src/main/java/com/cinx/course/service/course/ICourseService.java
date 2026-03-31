package com.cinx.course.service.course;

import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICourseService {
    CourseDetailResponse getCourseById(String courseId);
    List<CourseResponse> getCourseByIds(List<String> courseIds);
    Page<CourseResponse> getAllCourses(String query, String categoryId, String instructorId, Pageable pageable);
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse updateCourse(String courseId, UpdateCourseRequest request);
    void updateCourseRating(String courseId, Double rating);
}
