package com.cinx.learning.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.response.CourseDetailResponse;
import com.cinx.learning.dto.response.QuizLessonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "course", path = "/internal")
public interface CourseService {
    @GetMapping("/courses/{id}")
    ApiResponse<CourseDetailResponse> getCourseById(@PathVariable String id);

    @GetMapping("/quiz-lessons")
    ApiResponse<QuizLessonResponse> getQuizLessonById(@RequestParam String lessonId);

    @GetMapping("/video-lessons")
    ApiResponse<com.cinx.learning.dto.response.VideoLessonResponse> getVideoLessonById(@RequestParam String lessonId);
}
