package com.ceos.beatbuddy.domain.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FollowId implements Serializable {
    @Column(name = "followerId")
    private Long followerId;
    @Column(name = "followingId")
    private Long followingId;
}