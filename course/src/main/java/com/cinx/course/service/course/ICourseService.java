package com.cinx.course.service.course;

import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICourseService {
    CourseDetailResponse getCourseById(String courseId);
    List<CourseResponse> getCourseByIds(List<String> courseIds);
    Page<CourseResponse> getAllCourses(String query, String categoryId, String instructorId, int page, int size, String sort);
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse updateCourse(String courseId, UpdateCourseRequest request);
    void updateCourseRating(String courseId, Double rating);
}
