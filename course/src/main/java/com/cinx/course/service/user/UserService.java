package com.cinx.course.service.user;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.dto.response.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user", path = "/internal/users")
public interface UserService {

    @GetMapping("/{userId}")
    ApiResponse<UserDto> getInstructorById(@PathVariable String userId);

    @GetMapping("/ids")
    ApiResponse<List<UserDto>> getInstructorsByIds(@RequestParam List<String> ids);
}
