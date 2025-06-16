package com.ceos.beatbuddy.domain.scrapandlike.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostInteractionId implements Serializable {
    private Long memberId;
    private Long postId;
}
