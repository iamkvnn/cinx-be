package com.cinx.course.service.enrollment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.config.FeignConfig;
import com.cinx.course.dto.response.CheckEnrollmentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "enrollment", path = "/internal", configuration = FeignConfig.class)
public interface EnrollmentClient {
    @PostMapping("/enrollments/check")
    ApiResponse<List<CheckEnrollmentStatus>> checkEnrollmentStatus(@RequestHeader("X-User-Id") String userId, @RequestBody List<String> courseIds);
}
