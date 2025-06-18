package com.ceos.beatbuddy.domain.follow.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Follows")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow extends BaseTimeEntity {

    @EmbeddedId
    private FollowId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId") // FollowId.followerId 와 매핑
    @JoinColumn(name = "followerId")
    private Member follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingId") // FollowId.followingId 와 매핑
    @JoinColumn(name = "followingId")
    private Member following;
}