package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;

public interface NotificationSender {
    void send(String targetToken, NotificationPayload payload);
}
