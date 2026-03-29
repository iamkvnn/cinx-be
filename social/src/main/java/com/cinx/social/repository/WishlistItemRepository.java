package com.cinx.social.repository;

import com.cinx.social.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {
    List<WishlistItem> findByUserId(String userId);
    void deleteByUserIdAndCourseId(String userId, String courseId);
}
