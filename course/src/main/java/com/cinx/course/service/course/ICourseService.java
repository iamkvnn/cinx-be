package com.cinx.course.service.course;

import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.RejectCourseResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICourseService {
    CourseResponse getPublishedCourseById(String courseId);
    List<CourseResponse> getPublishedCourseByIds(List<String> courseIds);
    Page<CourseResponse> getAllPublishedCourses(String query, String categoryId, String instructorId, Integer rating, Integer priceFrom, Integer priceTo, int page, int size, String sort);
    CourseResponse getCourseById(String courseId);
    Page<CourseResponse> getAllCourses(String query, String categoryId, String instructorId, Integer rating, Integer priceFrom, Integer priceTo, CourseStatus status, int page, int size, String sort);
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse updateCourse(String courseId, UpdateCourseRequest request);
    CourseResponse submitCourse(String courseId);
    CourseResponse approveCourse(String courseId);
    CourseResponse rejectCourse(String courseId, RejectCourseRequest request);
    RejectCourseResponse getRejectReason(String courseId);
    void updateCourseRating(String courseId, Double rating);
    void increaseEnrollmentCount(String courseId);
}
