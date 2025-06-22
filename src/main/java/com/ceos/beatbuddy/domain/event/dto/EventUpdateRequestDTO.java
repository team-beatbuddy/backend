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

    private Boolean isVisible;

    private Boolean receiveInfo;
    private Boolean receiveName;
    private Boolean receiveGender;
    private Boolean receivePhoneNumber;
    private Boolean receiveTotalCount;
    private Boolean receiveSNSId;
    private Boolean receiveMoney;

    private List<String> deleteImageUrls;
}
