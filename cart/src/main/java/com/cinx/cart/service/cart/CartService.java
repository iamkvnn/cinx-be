package com.cinx.cart.service.cart;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.dto.response.CartItemResponse;
import com.cinx.cart.dto.response.CourseResponse;
import com.cinx.cart.model.CartItem;
import com.cinx.cart.repository.CartItemRepository;
import com.cinx.cart.service.course.CourseService;
import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.utils.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartItemRepository cartItemRepository;
    private final CourseService courseService;

    @Override
    public List<CartItemResponse> getCart() {
        String userId = AuthenticationUtil.extractUserId();
        List<CartItem> cartItems = cartItemRepository.findAllByUserId(userId);
        if (cartItems.isEmpty()) {
            return List.of();
        }
        Map<String, CourseResponse> courses = courseService
                .getCoursesByIds(cartItems
                        .parallelStream()
                        .map(CartItem::getCourseId)
                        .toList())
                .data()
                .stream()
                .collect(Collectors.toMap(CourseResponse::id, course -> course));
        return cartItems.stream()
                .filter(cartItem -> courses.containsKey(cartItem.getCourseId()))
                .map(cartItem -> new CartItemResponse(
                        cartItem.getId(),
                        courses.get(cartItem.getCourseId())
                ))
                .toList();
    }

    @Override
    public void addToCart(AddToCartRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        courseService.getCourseById(request.courseId());
        if (cartItemRepository.existsByUserIdAndCourseId(userId, request.courseId())) {
            throw new AlreadyExistException(ErrorCode.CART_ITEM_ALREADY_EXISTS, "Course already in cart");
        }
        cartItemRepository.save(
                CartItem.builder()
                        .userId(userId)
                        .courseId(request.courseId())
                        .build()
        );
    }

    @Transactional
    @Override
    public void removeFromCart(String itemId) {
        String userId = AuthenticationUtil.extractUserId();
        cartItemRepository.deleteByIdAndUserId(itemId, userId);
    }

    @Transactional
    @Override
    public void removeAllFromCartByIds(List<String> itemIds) {
        String userId = AuthenticationUtil.extractUserId();
        cartItemRepository.deleteAllByUserIdAndIdIn(userId, itemIds);
    }

    @Transactional
    @Override
    public void clearCart() {
        String userId = AuthenticationUtil.extractUserId();
        cartItemRepository.deleteAllByUserId(userId);
    }
}
