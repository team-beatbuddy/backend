package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCommentResponseDTO {
    private Long commentId;
    private Integer commentLevel;
    private String content;
    private String authorNickname;
    private boolean anonymous;
    private LocalDateTime createdAt;

    @JsonProperty("isAuthor")
    private Boolean isAuthor;

    @JsonProperty("isFollowing")
    private Boolean isFollowing;

    @JsonProperty("isBlockedMember")
    private Boolean isBlockedMember;

    @JsonProperty("isStaff")
    private Boolean isStaff;

    private Long writerId;

    public Boolean getIsBlockedMember() {
        return isBlockedMember;
    }

    public Boolean getIsStaff() {return isStaff;}
    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    public static EventCommentResponseDTO toDTO(EventComment eventComment, boolean isAuthor, boolean isFollowing, boolean isStaff, boolean isBlockedMember, String authorNickname) {
        return EventCommentResponseDTO.builder()
                .commentId(eventComment.getId())
                .commentLevel(eventComment.getLevel())
                .content(isBlockedMember ? "차단한 멤버의 댓글입니다." : eventComment.getContent())
                .authorNickname(authorNickname)
                .anonymous(eventComment.isAnonymous())
                .createdAt(eventComment.getCreatedAt())
                .isAuthor(isAuthor)
                .isFollowing(isFollowing)
                .isBlockedMember(isBlockedMember)
                .isStaff(isStaff)
                .writerId(eventComment.getAuthor().getId())
                .build();
    }
}