package com.cinx.notification.service.push;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.client.UserClient;
import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.service.notification.INotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final UserClient userClient;
    private final INotificationService notificationService;

    public void sendPushNotificationToUser(String userId, String title, String body) {
        try {
            ApiResponse<List<String>> response = userClient.getUserFcmTokens(userId);
            
            if (response != null && response.data() != null && !response.data().isEmpty()) {
                List<String> tokens = response.data();
                notificationService.sendNotification(new CreateNotificationRequest(
                        title,
                        body,
                        List.of(userId)
                ));
                
                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();

                MulticastMessage message = MulticastMessage.builder()
                        .addAllTokens(tokens)
                        .setNotification(notification)
                        .putData("click_action", "NOTIFICATION_CLICK") // Adjust based on your frontend
                        .build();

                FirebaseMessaging.getInstance().sendEachForMulticastAsync(message);
                log.info("Sent push notification to user {}, total tokens: {}", userId, tokens.size());
            } else {
                log.info("User {} has no FCM tokens registered. Skipping push notification.", userId);
            }
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
        }
    }
}