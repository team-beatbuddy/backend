package com.ceos.beatbuddy.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateRequestDTO {

    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;

    private Integer entranceFee; // 입장료
    private String entranceNotice; // 입장료 공지
    private String notice;
    private Boolean isFreeEntrance; // 무료 입장 여부

    private Boolean isVisible;

    private Boolean receiveInfo;
    private Boolean receiveName;
    private Boolean receiveGender;
    private Boolean receivePhoneNumber;
    private Boolean receiveTotalCount;
    private Boolean receiveSNSId;
    private Boolean receiveMoney;

    private String depositAccount;
    private Integer depositAmount;

    private List<String> deleteImageUrls;
    private String region;
}
