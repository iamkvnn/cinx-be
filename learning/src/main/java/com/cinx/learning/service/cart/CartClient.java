package com.cinx.learning.service.cart;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.request.AddToCartRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cart", path = "/internal/cart")
public interface CartClient {
    
    @PostMapping
    ApiResponse<Void> addToCart(@RequestBody AddToCartRequest request);
}
