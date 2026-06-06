package com.cinx.learning.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.response.CourseResponse;
import com.cinx.learning.dto.response.LessonResponse;
import com.cinx.learning.dto.response.QuizLessonResponse;
import com.cinx.learning.dto.response.VideoLessonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "course", path = "/internal")
public interface CourseService {
    @GetMapping("/courses/enrolled/{id}")
    ApiResponse<CourseResponse> getCourseById(@PathVariable String id);

    @GetMapping("/courses/enrolled/{id}/lessons")
    ApiResponse<List<String>> getCourseLessonIdsByCourseId(@PathVariable String id);

    @GetMapping("/courses/enrolled/{courseId}/lessons/{lessonId}")
    ApiResponse<LessonResponse> getEnrolledLessonById(@PathVariable String courseId, @PathVariable String lessonId);

    @GetMapping("/lessons/{lessonId}/instructor-access")
    ApiResponse<Boolean> isLessonInstructor(@PathVariable String lessonId, @RequestParam String userId);

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/quizzes")
    ApiResponse<QuizLessonResponse> getQuizLessonById(@PathVariable String courseId, @PathVariable String lessonId);

    @GetMapping("/courses/{courseId}/lessons/{lessonId}/videos")
    ApiResponse<VideoLessonResponse> getVideoLessonById(@PathVariable String courseId, @PathVariable String lessonId);

    @GetMapping("/video-questions/{id}/check-answer")
    ApiResponse<Boolean> checkVideoQuestionAnswer(@PathVariable("id") String id, @RequestParam("answer") String answer);
}
