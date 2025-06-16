package com.ceos.beatbuddy.domain.event.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventAttendanceId implements Serializable {
    private Long eventId;
    private Long memberId;
}