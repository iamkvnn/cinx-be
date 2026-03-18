package com.cinx.notification.mapper;

import com.cinx.notification.dto.response.UserNotificationResponse;
import com.cinx.notification.model.UserNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserNotificationMapper {
    @Mapping(target = "title", source = "entity.notification.title")
    @Mapping(target = "message", source = "entity.notification.message")
    UserNotificationResponse toDto(UserNotification entity);
}
