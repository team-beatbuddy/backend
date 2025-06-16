package com.ceos.beatbuddy.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventListResponseDTO {
    private String sort;
    private Integer page;
    private Integer size;
    private Integer totalSize;
    private List<EventResponseDTO> eventResponseDTOS;
}
