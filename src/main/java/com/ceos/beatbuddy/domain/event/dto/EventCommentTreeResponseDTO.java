package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCommentTreeResponseDTO {
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

    public Boolean getIsBlockedByMember() {
        return isBlockedMember;
    }
    public Boolean getIsStaff() {
        return isStaff;
    }
    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    private List<EventCommentResponseDTO> replies;

    public static EventCommentTreeResponseDTO toDTO(EventComment root, List<EventCommentResponseDTO> replies, boolean isAuthor, boolean isFollowing, boolean isStaff, boolean isBlockedMember, String authorNickname) {
        return EventCommentTreeResponseDTO.builder()
                .commentId(root.getId())
                .commentLevel(root.getLevel())
                .content(isBlockedMember ? "차단한 멤버의 댓글입니다." : root.getContent())
                .authorNickname(authorNickname)
                .anonymous(root.isAnonymous())
                .createdAt(root.getCreatedAt())
                .isAuthor(isAuthor)
                .isFollowing(isFollowing)
                .isBlockedMember(isBlockedMember)
                .writerId(root.getAuthor().getId())
                .isStaff(isStaff)
                .replies(replies)
                .build();
    }
}
