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

    @JsonProperty("isBlockedByWriter")
    private Boolean isBlockedByWriter;

    @JsonProperty("isStaff")
    private Boolean isStaff;

    private Long writerId;

    public Boolean getIsBlockedByWriter() {
        return isBlockedByWriter;
    }

    public Boolean getIsStaff() {return isStaff;}
    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    public static EventCommentResponseDTO toDTO(EventComment eventComment, boolean isAuthor, boolean isFollowing, boolean isStaff, boolean isBlockedByWriter) {
        return EventCommentResponseDTO.builder()
                .commentId(eventComment.getId())
                .commentLevel(eventComment.getLevel())
                .content(isBlockedByWriter ? "차단한 멤버의 댓글입니다." : eventComment.getContent())
                .authorNickname(eventComment.isAnonymous() ? "익명" : eventComment.getAuthor().getNickname())
                .anonymous(eventComment.isAnonymous())
                .createdAt(eventComment.getCreatedAt())
                .isAuthor(isAuthor)
                .isFollowing(isFollowing)
                .isBlockedByWriter(isBlockedByWriter)
                .isStaff(isStaff)
                .writerId(eventComment.getAuthor().getId())
                .build();
    }
}