package com.ceos.beatbuddy.domain.firebase.entity;

import com.ceos.beatbuddy.domain.firebase.FirebaseMessageType;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiverId", nullable = false)
    private Member receiver;

    @Enumerated(EnumType.STRING)
    private FirebaseMessageType type; // 예: EVENT_COMMENT_REPLY

    private String title;
    private String message;
    private String imageUrl;

    private boolean isRead = false;

    private LocalDateTime readAt;

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
