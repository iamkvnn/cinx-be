package com.cinx.notification.service.dispatch;

import com.cinx.notification.messaging.context.NotificationContext;

public interface INotificationDispatchService {
    void dispatch(NotificationContext context);
}
