package com.ceos.beatbuddy.domain.scrapandlike.entity;


import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventScrap extends BaseTimeEntity {

    @EmbeddedId
    private EventInteractionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "eventId")
    private Event event;

    public static EventScrap toEntity(Member member, Event event) {
        return EventScrap.builder()
                .id(new EventInteractionId(member.getId(), event.getId()))
                .member(member)
                .event(event)
                .build();
    }
}