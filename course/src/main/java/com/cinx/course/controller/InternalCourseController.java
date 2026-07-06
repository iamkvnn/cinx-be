package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.response.*;
import com.cinx.course.service.course.ICourseService;
import com.cinx.course.service.curriculum.ICurriculumService;
import com.cinx.course.service.lesson.ILessonService;
import com.cinx.course.service.quiz.IQuizService;
import com.cinx.course.service.video.IVideoService;
import com.cinx.course.service.videoquestion.IVideoQuestionService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API — called only by other services via Feign (service-to-service).
 * Not exposed externally; blocked at the gateway layer (/internal/** → denyAll).
 */
@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalCourseController {

    private final ICourseService courseService;
    private final ICurriculumService curriculumService;
    private final ILessonService lessonService;
    private final IQuizService quizService;
    private final IVideoService videoService;
    private final IVideoQuestionService videoQuestionService;

    @GetMapping("/courses/{id}")
    public ApiResponse<CourseResponse> getReadableCourseById(
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
            @PathVariable String id
    ) {
        return new ApiResponse<>(true, "Course fetched successfully", courseService.getReadableCourseById(currentUserId, id));
    }

    @GetMapping("/courses/{id}/lessons")
    public ApiResponse<List<String>> getCourseLessonIdsByCourseId(@PathVariable String id) {
        return new ApiResponse<>(true, "Course fetched successfully", lessonService.getEnrolledLessonIdsByCourseId(id));
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public ApiResponse<LessonResponse> getEnrolledLessonById(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return new ApiResponse<>(true, "Lesson fetched successfully", lessonService.getEnrolledLessonByCourseIdAndLessonId(courseId, lessonId));
    }

    @GetMapping("/lessons/{lessonId}/instructor-access")
    public ApiResponse<Boolean> isLessonInstructor(
            @PathVariable String lessonId,
            @RequestParam String userId
    ) {
        return new ApiResponse<>(true, "Instructor access checked successfully", lessonService.isLessonInstructor(lessonId, userId));
    }

    @GetMapping("/courses/{id}/curriculum")
    public ApiResponse<CourseCurriculumResponse> getCourseCurriculum(
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
            @PathVariable String id
    ) {
        return new ApiResponse<>(true, "Course curriculum fetched successfully", curriculumService.getReadableCurriculum(currentUserId, id));
    }

    @GetMapping("/courses/ids")
    public ApiResponse<List<CourseResponse>> getCoursesByIds(
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
            @RequestParam List<String> ids
    ) {
        return new ApiResponse<>(true, "Courses fetched successfully", courseService.getReadableCourseByIds(currentUserId, ids));
    }

    @GetMapping("/courses/search-ids")
    public ApiResponse<List<String>> searchReadableCourseIds(
            @RequestParam List<String> ids,
            @RequestParam String query
    ) {
        return new ApiResponse<>(true, "Course ids fetched successfully", courseService.searchReadableCourseIds(ids, query));
    }

    @PostMapping("/courses/{id}/update-rating")
    public ApiResponse<Void> updateCourseRating(@PathVariable String id, @RequestParam(required = false) Double rating) {
        courseService.updateCourseRating(id, rating);
        return new ApiResponse<>(true, "Rating updated successfully", null);
    }

    @PostMapping("/courses/{id}/increase-enrollment")
    public ApiResponse<Void> increaseEnrollmentCount(@PathVariable String id) {
        courseService.increaseEnrollmentCount(id);
        return new ApiResponse<>(true, "Enrollment count increased successfully", null);
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/quizzes")
    public ApiResponse<QuizLessonResponse> getQuizLessonById(
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return new ApiResponse<>(true, "Quiz lesson fetched successfully", quizService.getQuizByLessonId(currentUserId, courseId, lessonId));
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/videos")
    public ApiResponse<VideoLessonResponse> getVideoLessonById(
            @RequestHeader(value = "X-User-Id", required = false) String currentUserId,
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return new ApiResponse<>(true, "Video lesson fetched successfully", videoService.getReadableVideoByLessonId(currentUserId, courseId, lessonId));
    }

    @GetMapping("/video-questions/{id}/check-answer")
    public ApiResponse<Boolean> checkVideoQuestionAnswer(@PathVariable String id, @RequestParam String answer) {
        return new ApiResponse<>(true, "Answer checked successfully", videoQuestionService.checkAnswer(id, answer));
    }
}
