package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EventResponseDTO {
    private Long eventId;
    private String title;
    private String content;
    private String image;

    public static EventResponseDTO toDTO(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .image(event.getThumbImage())
                .build();
    }
}
