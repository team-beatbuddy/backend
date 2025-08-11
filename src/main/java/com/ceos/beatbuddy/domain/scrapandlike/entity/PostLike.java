package com.ceos.beatbuddy.domain.scrapandlike.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike extends BaseTimeEntity {

    @EmbeddedId
    private PostInteractionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @MapsId("memberId")
    @JoinColumn(name = "memberId", nullable = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "postId")
    private Post post;

    public static PostLike toEntity(Member member, Post post) {
        return PostLike.builder()
                .id(new PostInteractionId(member.getId(), post.getId()))
                .member(member)
                .post(post)
                .build();
    }

    public Long getPostId() {
        return this.post.getId();
    }
}
