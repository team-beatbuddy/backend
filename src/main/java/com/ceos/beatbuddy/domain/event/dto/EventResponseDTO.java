package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponseDTO {
    private Long eventId;
    private String title;
    private String content;
    private String image;
    private String dDay;
    private Boolean liked;
    private Boolean scrapped;
    private String location;

    private Integer likes;
    private Integer views;
    private Integer scraps;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean receiveInfo;
    private Boolean receiveName; // 이름 받을 건지
    private Boolean receiveGender; // 성별 받을 건지
    private Boolean receivePhoneNumber; // 전화번호 받을 건지
    private Boolean receiveTotalCount; // 동행 인원 받을 건지
    private Boolean receiveSNSId; // sns id 받을 건지
    private Boolean receiveMoney; // 예약금 받을 건지


    public static EventResponseDTO toDTO(Event event, Boolean liked) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .image(event.getThumbImage())
                .views(event.getViews())
                .likes(event.getLikes())
                .liked(liked)
                .receiveInfo(event.isReceiveInfo())
                .receiveName(event.isReceiveName())
                .receiveGender(event.isReceiveGender())
                .receivePhoneNumber(event.isReceivePhoneNumber())
                .receiveMoney(event.isReceiveMoney())
                .receiveSNSId(event.isReceiveSNSId())
                .build();
    }

    public static EventResponseDTO toUpcomingListDTO(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .dDay("D-" + ChronoUnit.DAYS.between(LocalDate.now(), event.getStartDate()))
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .image(event.getThumbImage())
                .likes(event.getLikes())
                .views(event.getViews())
                .location(event.getLocation())
                .build();
    }

    public static EventResponseDTO toPastListDTO(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .image(event.getThumbImage())
                .likes(event.getLikes())
                .views(event.getViews())
                .location(event.getLocation())
                .build();
    }

    public static EventResponseDTO toUpcomingDTO(Event event, boolean liked) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .dDay("D-" + ChronoUnit.DAYS.between(LocalDate.now(), event.getStartDate()))
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .image(event.getThumbImage())
                .likes(event.getLikes())
                .liked(liked)
                .views(event.getViews())
                .build();
    }
}
