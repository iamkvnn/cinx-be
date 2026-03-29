package com.cinx.cart.repository;

import com.cinx.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    void deleteAllByUserId(String userId);

    void deleteByIdAndUserId(String itemId, String userId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.id IN :itemIds")
    void deleteAllByUserIdAndIdIn(String userId, List<String> itemIds);

    List<CartItem> findAllByUserId(String userId);

    boolean existsByUserIdAndCourseId(String userId, String s);
}
