package com.cinx.learning.service.cart;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.request.AddToCartRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cart", path = "/api/v1")
public interface CartClient {
    
    @PostMapping("/cart")
    ApiResponse<Void> addToCart(@RequestBody AddToCartRequest request);
}
