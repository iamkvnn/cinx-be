package com.cinx.social.service.course;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.response.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "course", path = "/internal")
public interface CourseService {
    @GetMapping("/courses/{id}")
    ApiResponse<CourseResponse> getCourseById(@PathVariable String id);

    @GetMapping("/courses/ids")
    ApiResponse<List<CourseResponse>> getCoursesByIds(@RequestParam List<String> ids);

    @PostMapping("/courses/{id}/update-rating")
    ApiResponse<Void> updateCourseRating(@PathVariable("id") String id, @RequestParam("rating") Double rating);
}
