package com.cinx.enrollment.service.cart;

import com.cinx.enrollment.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "cart", path = "/internal/cart", configuration = FeignConfig.class)
public interface CartService {
    @DeleteMapping("/ids")
    void removeAllFromCartByIds(@RequestHeader("X-User-Id") String userId, @RequestParam List<String> itemIds);
}
