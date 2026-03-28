package com.cinx.social.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WishlistEvent {
    private String userId;
    private String courseId;
    private boolean added;
}
