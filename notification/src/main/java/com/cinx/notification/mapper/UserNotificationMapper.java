package com.cinx.notification.mapper;

import com.cinx.notification.dto.response.UserNotificationResponse;
import com.cinx.notification.model.UserNotification;
import com.cinx.notification.utils.NotificationJson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = NotificationJson.class)
public interface UserNotificationMapper {
    @Mapping(target = "title", source = "entity.notification.title")
    @Mapping(target = "message", source = "entity.notification.message")
    @Mapping(target = "type", source = "entity.notification.type")
    @Mapping(target = "referenceId", source = "entity.notification.referenceId")
    @Mapping(target = "actionUrl", source = "entity.notification.actionUrl")
    @Mapping(target = "metadata", expression = "java(NotificationJson.read(entity.getNotification().getMetadataJson()))")
    UserNotificationResponse toDto(UserNotification entity);
}
