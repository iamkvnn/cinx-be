package com.cinx.cart.controller;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.service.cart.ICartService;
import com.cinx.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API — called only by other services via Feign (service-to-service).
 * Not exposed externally; blocked at the gateway layer (/internal/** → denyAll).
 */
@Hidden
@RestController
@RequestMapping("/internal/cart")
@RequiredArgsConstructor
public class InternalCartController {

    private final ICartService cartService;

    @PostMapping
    public ApiResponse<Void> addToCart(@RequestBody AddToCartRequest request) {
        cartService.addToCart(request);
        return new ApiResponse<>(true, "Item added to cart successfully", null);
    }

    @DeleteMapping("/ids")
    public ApiResponse<Void> removeAllFromCartByIds(@RequestParam List<String> itemIds) {
        cartService.removeAllFromCartByIds(itemIds);
        return new ApiResponse<>(true, "Items removed from cart successfully", null);
    }
}
