package com.cinx.social.client;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.response.CheckEnrollmentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "enrollment", path = "/internal")
public interface EnrollmentClient {
    @PostMapping("/enrollments/check")
    ApiResponse<List<CheckEnrollmentStatus>> checkEnrollmentStatus(@RequestBody List<String> courseIds);
}
