package com.cinx.learning.service.user;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.response.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user", path = "/api/v1/users")
public interface UserService {
    @GetMapping("/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable("id") String id);

    @PostMapping("/{id}/add-xp")
    ApiResponse<UserDto> addXp(@PathVariable("id") String id, @RequestParam("amount") Integer amount);
}