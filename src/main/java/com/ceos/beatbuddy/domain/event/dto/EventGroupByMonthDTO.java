package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class EventGroupByMonthDTO {
    private String yearMonth;
    private List<EventResponseDTO> events;

    public static EventGroupByMonthDTO groupByMonth(String yyyyMM, List<Event> events) {
        List<EventResponseDTO> list = events.stream()
                .map(event -> EventResponseDTO.builder()
                        .eventId(event.getId())
                        .title(event.getTitle())
                        .content(event.getContent())
                        .startDate(event.getStartDate())
                        .endDate(event.getEndDate())
                        .thumbImage(Optional.ofNullable(event.getThumbImage()).orElse(""))
                        .likes(event.getLikes())
                        .views(event.getViews())
                        .location(event.getLocation())
                        .build())
                .toList();

        return EventGroupByMonthDTO.builder()
                .yearMonth(yyyyMM)
                .events(list)
                .build();
    }
}