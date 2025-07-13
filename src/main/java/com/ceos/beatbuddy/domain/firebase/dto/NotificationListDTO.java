package com.ceos.beatbuddy.domain.firebase.dto;

import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class NotificationListDTO {
    private String title;
    private String message;
    private String imageUrl;
    private String type;
    private Long memberId;
    @JsonProperty("isRead")
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationListDTO from(Notification entity) {
        return NotificationListDTO.builder()
                .title(entity.getTitle())
                .message(entity.getMessage())
                .imageUrl(entity.getImageUrl())
                .type(entity.getType().name())
                .memberId(entity.getReceiver().getId())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }
}
