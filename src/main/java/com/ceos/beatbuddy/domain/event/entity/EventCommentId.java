package com.ceos.beatbuddy.domain.event.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventCommentId implements Serializable {
    private Long id;     // 댓글 그룹 ID
    private Integer level; // 계층 (0: 본 댓글, 1+ 대댓글)
}