package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponseDTO {
    private Long eventId;
    private String title;
    private String content;
    private List<String> images;

    private String thumbImage; // 썸네일

    private String dDay;

    private Boolean liked;
    private String location;

    private Integer likes;
    private Integer views;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private Boolean receiveInfo;
    private Boolean receiveName; // 이름 받을 건지
    private Boolean receiveGender; // 성별 받을 건지
    private Boolean receivePhoneNumber; // 전화번호 받을 건지
    private Boolean receiveTotalCount; // 동행 인원 받을 건지
    private Boolean receiveSNSId; // sns id 받을 건지
    private Boolean receiveMoney; // 예약금 받을 건지

    private String depositAccount;
    private Integer depositAmount;

    private Integer entranceFee; // 입장료
    private String entranceNotice; // 입장료 공지
    private String notice;
    @JsonProperty("isFreeEntrance")
    private Boolean isFreeEntrance; // 무료 입장 여부

    private String region;

    @JsonProperty("isAttending")
    private Boolean isAttending;


    @JsonProperty("isAuthor")
    private Boolean isAuthor;


    public Boolean getIsFreeEntrance() {
        return isFreeEntrance;
    }
    public Boolean getIsAttending() {
        return isAttending;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }


    public static EventResponseDTO toDTO(Event event, boolean liked, boolean isAuthor, boolean isAttending) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .images(Optional.ofNullable(event.getImageUrls()).orElseGet(ArrayList::new))
                .views(event.getViews())
                .likes(event.getLikes())
                .liked(liked)
                .location(event.getLocation())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .receiveInfo(event.isReceiveInfo())
                .receiveName(event.isReceiveName())
                .receiveGender(event.isReceiveGender())
                .receivePhoneNumber(event.isReceivePhoneNumber())
                .receiveMoney(event.isReceiveMoney())
                .receiveSNSId(event.isReceiveSNSId())
                .depositAccount(Optional.ofNullable(event.getDepositAccount()).orElse(""))
                .depositAmount(Optional.ofNullable(event.getDepositAmount()).orElse(0))
                .isAuthor(isAuthor)
                .isAttending(isAttending)
                .entranceFee(event.getEntranceFee())
                .entranceNotice(Optional.ofNullable(event.getEntranceNotice()).orElse(""))
                .notice(Optional.ofNullable(event.getNotice()).orElse(""))
                .isFreeEntrance(event.isFreeEntrance())
                .region(Optional.of(event.getRegion().name()).orElse(""))
                .build();
    }

    public static EventResponseDTO toListDTO(Event event, boolean isAuthor, boolean liked, boolean isAttending) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .thumbImage(Optional.ofNullable(event.getThumbImage()).orElse(""))
                .likes(event.getLikes())
                .views(event.getViews())
                .location(event.getLocation())
                .isAuthor(isAuthor)
                .liked(liked)
                .isAttending(isAttending)
                .isFreeEntrance(event.isFreeEntrance())
                .region(Optional.ofNullable(event.getRegion()).map(Enum::name).orElse(""))
                .build();
    }
}
