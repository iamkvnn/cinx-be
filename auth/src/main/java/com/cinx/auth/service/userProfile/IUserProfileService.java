package com.cinx.auth.service.userProfile;

import com.cinx.auth.dto.request.CreateUserProfileRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user", path = "/api/v1")
public interface IUserProfileService {
    @PostMapping("/users")
    void createUser(@RequestBody CreateUserProfileRequest user);
}
