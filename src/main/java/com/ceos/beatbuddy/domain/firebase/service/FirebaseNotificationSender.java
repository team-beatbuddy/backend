package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationSender implements NotificationSender {

    @Override
    public void send(String targetToken, NotificationPayload payload) {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(payload.getTitle())
                .setBody(payload.getBody());

        if (payload.getImageUrl() != null) {
            notificationBuilder.setImage(payload.getImageUrl());
        }

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(notificationBuilder.build())
                .putAllData(payload.getData())
                .build();

        FirebaseMessaging.getInstance().sendAsync(message);
    }
}
