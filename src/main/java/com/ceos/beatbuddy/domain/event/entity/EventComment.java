package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 단일 기본키

    private Long parentId; // null이면 댓글, 아니면 대댓글

    private Integer level; // 0: 댓글, 1+: 대댓글

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId")
    private Member author;

    private boolean anonymous;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
