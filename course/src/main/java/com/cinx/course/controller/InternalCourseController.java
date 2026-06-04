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
    public ApiResponse<CourseResponse> getCourseById(@PathVariable String id) {
        return new ApiResponse<>(true, "Course fetched successfully", courseService.getPublishedCourseById(id));
    }

    @GetMapping("/courses/enrolled/{id}")
    public ApiResponse<CourseResponse> getEnrolledCourseById(@PathVariable String id) {
        return new ApiResponse<>(true, "Course fetched successfully", courseService.getEnrolledCourseById(id));
    }

    @GetMapping("/courses/{id}/lessons")
    public ApiResponse<List<String>> getCourseLessonIdsByCourseId(@PathVariable String id) {
        return new ApiResponse<>(true, "Course fetched successfully", lessonService.getLessonIdsByCourseId(id));
    }

    @GetMapping("/courses/enrolled/{id}/lessons")
    public ApiResponse<List<String>> getEnrolledCourseLessonIdsByCourseId(@PathVariable String id) {
        return new ApiResponse<>(true, "Course fetched successfully", lessonService.getEnrolledLessonIdsByCourseId(id));
    }

    @GetMapping("/courses/{id}/curriculum")
    public ApiResponse<CourseCurriculumResponse> getCourseCurriculum(@PathVariable String id) {
        return new ApiResponse<>(true, "Course curriculum fetched successfully", curriculumService.getPublishedCurriculum(id));
    }

    @GetMapping("/courses/enrolled/{id}/curriculum")
    public ApiResponse<CourseCurriculumResponse> getEnrolledCourseCurriculum(@PathVariable String id) {
        return new ApiResponse<>(true, "Course curriculum fetched successfully", curriculumService.getEnrolledCurriculum(id));
    }

    @GetMapping("/courses/ids")
    public ApiResponse<List<CourseResponse>> getCoursesByIds(@RequestParam List<String> ids) {
        return new ApiResponse<>(true, "Courses fetched successfully", courseService.getPublishedCourseByIds(ids));
    }

    @GetMapping("/courses/enrolled/ids")
    public ApiResponse<List<CourseResponse>> getEnrolledCoursesByIds(@RequestParam List<String> ids) {
        return new ApiResponse<>(true, "Courses fetched successfully", courseService.getEnrolledCourseByIds(ids));
    }

    @PostMapping("/courses/{id}/update-rating")
    public ApiResponse<Void> updateCourseRating(@PathVariable String id, @RequestParam Double rating) {
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
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return new ApiResponse<>(true, "Quiz lesson fetched successfully", quizService.getQuizByLessonId(courseId, lessonId));
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/videos")
    public ApiResponse<VideoLessonResponse> getVideoLessonById(
            @PathVariable String courseId,
            @PathVariable String lessonId
    ) {
        return new ApiResponse<>(true, "Video lesson fetched successfully", videoService.getVideoByLessonId(courseId, lessonId));
    }

    @GetMapping("/video-questions/{id}/check-answer")
    public ApiResponse<Boolean> checkVideoQuestionAnswer(@PathVariable String id, @RequestParam String answer) {
        return new ApiResponse<>(true, "Answer checked successfully", videoQuestionService.checkAnswer(id, answer));
    }
}
