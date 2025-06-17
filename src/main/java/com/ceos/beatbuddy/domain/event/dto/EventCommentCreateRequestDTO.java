package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.member.entity.Member;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventCommentCreateRequestDTO {
    @NotNull(message = "내용은 필수입니다.")
    private String content;
    private boolean anonymous;
    private Long parentCommentId; // 없으면 최상위 댓글, 있으면 대댓글

    public static EventComment toEntity(
            EventCommentCreateRequestDTO dto,
            Event event,
            Member member,
            Integer level
    ) {
        return EventComment.builder()
                .event(event)
                .author(member)
                .content(dto.getContent())
                .anonymous(dto.isAnonymous())
                .level(level)
                .build();
    }
}