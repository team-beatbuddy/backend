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
    private Long writerId;

    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    private List<EventCommentResponseDTO> replies;

    public static EventCommentTreeResponseDTO toDTO(EventComment root, List<EventCommentResponseDTO> replies, boolean isAuthor, boolean isFollowing) {
        return EventCommentTreeResponseDTO.builder()
                .commentId(root.getId())
                .commentLevel(root.getLevel())
                .content(root.getContent())
                .authorNickname(root.isAnonymous() ? "익명" : root.getAuthor().getNickname())
                .anonymous(root.isAnonymous())
                .createdAt(root.getCreatedAt())
                .isAuthor(isAuthor)
                .isFollowing(isFollowing)
                .writerId(root.getAuthor().getId())
                .replies(replies)
                .build();
    }
}
