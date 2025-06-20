package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventComment;
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
    private List<EventCommentResponseDTO> replies;

    public static EventCommentTreeResponseDTO toDTO(EventComment root, List<EventCommentResponseDTO> replies) {
        return EventCommentTreeResponseDTO.builder()
                .commentId(root.getId())
                .commentLevel(root.getLevel())
                .content(root.getContent())
                .authorNickname(root.isAnonymous() ? "익명" : root.getAuthor().getNickname())
                .anonymous(root.isAnonymous())
                .createdAt(root.getCreatedAt())
                .replies(replies)
                .build();
    }
}
