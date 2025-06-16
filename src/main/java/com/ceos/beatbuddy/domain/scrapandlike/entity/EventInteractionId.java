package com.ceos.beatbuddy.domain.scrapandlike.entity;


import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventInteractionId implements Serializable {
    private Long memberId;
    private Long eventId;
}
