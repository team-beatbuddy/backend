package com.ceos.beatbuddy.domain.firebase.dto;

import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "알림 리스트 DTO")
public class NotificationListDTO {
    @Schema(description = "알림 ID", example = "1")
    private Long id;
    @Schema(description = "알림 제목", example = "새로운 이벤트가 등록되었습니다.")
    private String title;
    @Schema(description = "알림 내용", example = "우리의 새로운 이벤트에 참여해보세요!")
    private String message;
    @Schema(description = "알림 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
    @Schema(description = "알림 유형", example = "EVENT")
    private String type;
    @Schema(description = "알림과 연관된 본문의 ID (이벤트, 댓글 등)", example = "1")
    private Long contentId;
    @Schema(description = "수신자 회원 ID", example = "123")
    private Long memberId;
    @Schema(description = "알림 읽음 여부", example = "true")
    @JsonProperty("isRead")
    private Boolean isRead;
    @Schema(description = "알림 생성 시간", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "알림 읽음 시간", example = "2023-10-01T12:05:00")
    private LocalDateTime readAt;

    public static NotificationListDTO from(Notification entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Notification entity cannot be null");
        }

        return NotificationListDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .imageUrl(entity.getImageUrl())
                .type(entity.getType().name())
                .contentId(entity.getContentId())
                .memberId(entity.getReceiver().getId())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }
}
