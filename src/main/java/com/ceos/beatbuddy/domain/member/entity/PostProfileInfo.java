package com.ceos.beatbuddy.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostProfileInfo {
    private String postProfileNickname;
    private String postProfileImageUrl;
    
    @Column
    private LocalDateTime postProfileNicknameChangedAt;

    @Column(nullable = false)
    private int postProfileNicknameChangeCount = 0;

    @Version
    @Column(nullable = false)
    private Long postProfileVersion = 0L;

    @Builder.Default
    private Boolean setNewPostProfileNickname = false;

    public static PostProfileInfo from(String postProfileNickname, String postProfileImage) {
        return PostProfileInfo.builder()
                .postProfileNickname(postProfileNickname)
                .postProfileImageUrl(postProfileImage)
                .build();
    }
}
