package com.cinx.course.service.course;

import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.InstructorCourseSummaryResponse;
import com.cinx.course.dto.response.RejectCourseResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICourseService {
    CourseResponse getReadableCourseById(String currentUserId, String courseId);
    List<CourseResponse> getReadableCourseByIds(String currentUserId, List<String> courseIds);
    Page<CourseResponse> getAllPublishedCourses(String query, String categoryId, String instructorId, Integer rating, Integer priceFrom, Integer priceTo, int page, int size, String sort);
    CourseResponse getEditableDraftCourseById(String currentUserId, String courseId);
    InstructorCourseSummaryResponse getInstructorCourseSummary(String instructorId);
    Page<CourseResponse> getAllCourses(String query, String categoryId, String instructorId, Integer rating, Integer priceFrom, Integer priceTo, CourseStatus status, CoursePublishStatus publishStatus, int page, int size, String sort);
    CourseResponse createCourse(String currentUserId, CreateCourseRequest request);
    CourseResponse updateCourse(String currentUserId, String courseId, UpdateCourseRequest request);
    CourseResponse submitCourse(String currentUserId, String courseId);
    CourseResponse archiveCourse(String currentUserId, String courseId);
    CourseResponse unarchiveCourse(String currentUserId, String courseId);
    CourseResponse approveCourse(String courseId);
    CourseResponse rejectCourse(String courseId, RejectCourseRequest request);
    RejectCourseResponse getRejectReason(String courseId);
    void updateCourseRating(String courseId, Double rating);
    void increaseEnrollmentCount(String courseId);
    void replayRecommendationEvents();
}
