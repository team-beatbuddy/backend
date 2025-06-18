package com.ceos.beatbuddy.domain.follow.dto;

import com.ceos.beatbuddy.domain.follow.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponseDTO {
    private Long followerId;
    private Long followingId;

    public static FollowResponseDTO toDTO(Follow follow) {
        return FollowResponseDTO.builder()
                .followerId(follow.getFollower().getId())
                .followingId(follow.getFollowing().getId())
                .build();
    }
}
