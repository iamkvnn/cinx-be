package com.cinx.notification.client;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.dto.response.course.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course", path = "/internal/courses")
public interface CourseClient {
    @GetMapping("/{id}")
    ApiResponse<CourseResponse> getCourseById(@PathVariable("id") String id);
}