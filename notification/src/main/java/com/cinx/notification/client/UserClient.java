package com.cinx.notification.client;

import com.cinx.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user", path = "/api/v1/users")
public interface UserClient {
    
    @GetMapping("/{userId}/fcm-tokens")
    ApiResponse<List<String>> getUserFcmTokens(@PathVariable("userId") String userId);
}