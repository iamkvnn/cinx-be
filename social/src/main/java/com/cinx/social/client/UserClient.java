package com.cinx.social.client;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.response.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user", path = "/internal")
public interface UserClient {
    @GetMapping("/users/ids")
    ApiResponse<List<UserSummaryResponse>> getUsersByIds(@RequestParam List<String> ids);
}
