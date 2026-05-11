package com.cinx.enrollment.service.user;

import com.cinx.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "user", path = "/internal/users")
public interface UserService {
    @GetMapping("/metrics/total-count")
    ApiResponse<Long> getTotalUsersCount();

    @GetMapping("/metrics/new-count")
    ApiResponse<Long> getNewUsersCount(
            @RequestParam("start") String start,
            @RequestParam("end") String end
    );
}
