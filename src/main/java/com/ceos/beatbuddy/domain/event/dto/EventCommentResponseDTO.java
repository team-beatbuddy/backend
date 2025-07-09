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
    private Long writerId;

    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    public static EventCommentResponseDTO toDTO(EventComment eventComment, boolean isAuthor, boolean isFollowing) {
        return EventCommentResponseDTO.builder()
                .commentId(eventComment.getId())
                .commentLevel(eventComment.getLevel())
                .content(eventComment.getContent())
                .authorNickname(eventComment.isAnonymous() ? "익명" : eventComment.getAuthor().getNickname())
                .anonymous(eventComment.isAnonymous())
                .createdAt(eventComment.getCreatedAt())
                .isAuthor(isAuthor)
                .isFollowing(isFollowing)
                .writerId(eventComment.getAuthor().getId())
                .build();
    }
}