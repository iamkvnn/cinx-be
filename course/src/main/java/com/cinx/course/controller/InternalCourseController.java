package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.dto.response.VideoLessonResponse;
import com.cinx.course.service.course.ICourseService;
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
    private final IQuizService quizService;
    private final IVideoService videoService;
    private final IVideoQuestionService videoQuestionService;

    @GetMapping("/courses/{id}")
    public ApiResponse<CourseDetailResponse> getCourseById(@PathVariable String id) {
        return new ApiResponse<>(true, "Course fetched successfully", courseService.getCourseById(id));
    }

    @GetMapping("/courses/ids")
    public ApiResponse<List<CourseResponse>> getCoursesByIds(@RequestParam List<String> ids) {
        return new ApiResponse<>(true, "Courses fetched successfully", courseService.getCourseByIds(ids));
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

    @GetMapping("/quiz-lessons")
    public ApiResponse<QuizLessonResponse> getQuizLessonById(@RequestParam String lessonId) {
        return new ApiResponse<>(true, "Quiz lesson fetched successfully", quizService.getQuizByLessonId(lessonId));
    }

    @GetMapping("/video-lessons")
    public ApiResponse<VideoLessonResponse> getVideoLessonById(@RequestParam String lessonId) {
        return new ApiResponse<>(true, "Video lesson fetched successfully", videoService.getVideoByLessonId(lessonId));
    }

    @GetMapping("/video-questions/{id}/check-answer")
    public ApiResponse<Boolean> checkVideoQuestionAnswer(@PathVariable String id, @RequestParam String answer) {
        return new ApiResponse<>(true, "Answer checked successfully", videoQuestionService.checkAnswer(id, answer));
    }
}
