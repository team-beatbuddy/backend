package com.ceos.beatbuddy.domain.member.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostProfileInfo {
    private String postProfileNickname;
    private String postProfileImageUrl;

    public static PostProfileInfo from(String postProfileNickname, String postProfileImage) {
        return PostProfileInfo.builder()
                .postProfileNickname(postProfileNickname)
                .postProfileImageUrl(postProfileImage)
                .build();
    }
}
