package com.cinx.enrollment.service.cart;

import com.cinx.enrollment.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "cart", path = "/api/v1/cart", configuration = FeignConfig.class)
public interface CartService {
    @DeleteMapping("/ids")
    void removeAllFromCartByIds(@RequestParam List<String> itemIds);
}
