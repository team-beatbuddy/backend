package com.ceos.beatbuddy.domain.firebase.config;

import com.ceos.beatbuddy.domain.firebase.service.FirebaseNotificationSender;
import com.ceos.beatbuddy.domain.firebase.service.KafkaNotificationService;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class NotificationSenderConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(value = "notification.sender.type", havingValue = "kafka", matchIfMissing = true)
    public NotificationSender kafkaNotificationSender(KafkaNotificationService kafkaNotificationService) {
        return kafkaNotificationService;
    }

    @Bean
    @ConditionalOnProperty(value = "notification.sender.type", havingValue = "direct")
    public NotificationSender directNotificationSender(FirebaseNotificationSender firebaseNotificationSender) {
        return firebaseNotificationSender;
    }
}