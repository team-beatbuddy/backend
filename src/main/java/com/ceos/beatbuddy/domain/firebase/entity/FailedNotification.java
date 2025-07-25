package com.ceos.beatbuddy.domain.firebase.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "FailedNotification")
public class FailedNotification {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String targetToken;

    @Lob
    private String payloadJson;

    private int retryCount;

    private LocalDateTime lastTriedAt;

    private boolean resolved;
    private String reason;

    public static FailedNotification toEntity(String targetToken, String payloadJson, String reason) {
        return FailedNotification.builder()
                .targetToken(targetToken)
                .payloadJson(payloadJson)
                .retryCount(0)
                .lastTriedAt(LocalDateTime.now())
                .resolved(false)
                .reason(reason)
                .build();
    }

    public void setResolved(boolean b) {
        this.resolved = b;
    }

    public void setRetryCount(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Retry count cannot be negative");
        }
        this.retryCount = i;
    }

    public void setLastTriedAt(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Last tried time cannot be null");
        }
        this.lastTriedAt = now;
    }

    public void setFailReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Fail reason cannot be null or empty");
        }
        this.reason = reason;
    }
}
