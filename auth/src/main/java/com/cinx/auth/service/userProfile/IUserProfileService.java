package com.cinx.auth.service.userProfile;

import com.cinx.auth.dto.request.CreateUserProfileRequest;
import com.cinx.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user", path = "/internal/users")
public interface IUserProfileService {
    @PostMapping
    void createUser(@RequestBody CreateUserProfileRequest user);

    @GetMapping("/{userId}/instructor-verified")
    ApiResponse<Boolean> checkInstructorVerified(@PathVariable String userId);

    @PostMapping("/{userId}/toggle-ban")
    void toggleBanUser(@PathVariable String userId);

    @PostMapping("/{userId}/last-access")
    void updateLastAccess(@PathVariable String userId);
}
