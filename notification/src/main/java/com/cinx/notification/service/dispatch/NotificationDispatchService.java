package com.cinx.notification.service.dispatch;

import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.strategy.NotificationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService implements INotificationDispatchService {

    private final NotificationFactory notificationFactory;

    @Override
    public void dispatch(NotificationContext context) {
        if (context == null || context.channels() == null || context.channels().isEmpty()) {
            log.warn("NotificationDispatchService received empty context, skipping.");
            return;
        }

        Map<String, Map<String, Object>> payloadsByChannel = Map.of(
                "EMAIL",  context.emailPayload()  != null ? context.emailPayload()  : Map.of(),
                "IN_APP", context.inAppPayload()  != null ? context.inAppPayload()  : Map.of(),
                "PUSH",   context.pushPayload()   != null ? context.pushPayload()   : Map.of()
        );

        for (String channel : context.channels()) {
            try {
                Map<String, Object> payload = payloadsByChannel.getOrDefault(channel, Map.of());
                if (payload.isEmpty()) {
                    log.warn("No payload provided for channel '{}', skipping.", channel);
                    continue;
                }
                notificationFactory.getStrategy(channel).send(payload);
            } catch (Exception e) {
                // Fail-fast per channel so one bad channel does not block others
                log.error("Failed to dispatch notification via channel '{}': {}", channel, e.getMessage(), e);
            }
        }
    }
}
