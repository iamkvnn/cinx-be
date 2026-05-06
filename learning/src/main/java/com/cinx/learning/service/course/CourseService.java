package com.cinx.learning.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.response.CourseDetailResponse;
import com.cinx.learning.dto.response.QuizLessonResponse;
import com.cinx.learning.dto.response.VideoLessonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "course", path = "/internal")
public interface CourseService {
    @GetMapping("/courses/{id}")
    ApiResponse<CourseDetailResponse> getCourseById(@PathVariable String id);

    @GetMapping("/courses/{id}/lessons")
    ApiResponse<List<String>> getCourseLessonIdsByCourseId(@PathVariable String id);

    @GetMapping("/quiz-lessons")
    ApiResponse<QuizLessonResponse> getQuizLessonById(@RequestParam String lessonId);

    @GetMapping("/video-lessons")
    ApiResponse<VideoLessonResponse> getVideoLessonById(@RequestParam String lessonId);

    @GetMapping("/video-questions/{id}/check-answer")
    ApiResponse<Boolean> checkVideoQuestionAnswer(@PathVariable("id") String id, @RequestParam("answer") String answer);
}
