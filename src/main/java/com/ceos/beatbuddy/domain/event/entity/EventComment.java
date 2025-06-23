package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@IdClass(EventCommentId.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class EventComment extends BaseTimeEntity {

    @Id
    private Long id; // 댓글 그룹 ID (댓글 thread 기준)

    @Id
    private Integer level; // 대댓글 깊이

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorId")
    private Member author;

    private boolean anonymous;

    // 댓글 수정 구현
    public void updateContent(String content) {
        this.content = content;
    }

    public void updateAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}