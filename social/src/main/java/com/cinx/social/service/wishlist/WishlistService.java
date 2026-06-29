package com.cinx.social.service.wishlist;

import com.cinx.social.dto.request.AddToWishlistRequest;
import com.cinx.social.dto.response.WishlistItemResponse;
import com.cinx.social.mapper.WishlistItemMapper;
import com.cinx.social.messaging.WishlistEventProducer;
import com.cinx.social.messaging.event.WishlistEvent;
import com.cinx.social.model.WishlistItem;
import com.cinx.social.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService implements IWishlistService{
    private final WishlistItemRepository wishlistItemRepository;
    private final WishlistItemMapper wishlistItemMapper;
    private final WishlistEventProducer wishlistEventProducer;

    @Override
    public List<WishlistItemResponse> getWishlistByUserId(String userId) {
        return wishlistItemRepository.findByUserId(userId).stream()
                .map(wishlistItemMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void addToWishlist(String userId, AddToWishlistRequest request) {
        wishlistItemRepository.save(WishlistItem.builder()
                .userId(userId)
                .courseId(request.courseId())
                .build());
        wishlistEventProducer.publishWishlistAddedEvent(new WishlistEvent(userId, request.courseId(), true));
    }

    @Transactional
    @Override
    public void removeFromWishlist(String userId, String courseId) {
        wishlistItemRepository.deleteByUserIdAndCourseId(userId, courseId);
        wishlistEventProducer.publishWishlistRemovedEvent(new WishlistEvent(userId, courseId, false));
    }
}
