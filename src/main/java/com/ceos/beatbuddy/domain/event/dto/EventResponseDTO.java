package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EventResponseDTO {
    private Long eventId;
    private String title;
    private String content;
    private String image;
    private boolean receiveInfo;
    private boolean receiveName; // 이름 받을 건지
    private boolean receiveGender; // 성별 받을 건지
    private boolean receivePhoneNumber; // 전화번호 받을 건지
    private boolean receiveTotalCount; // 동행 인원 받을 건지
    private boolean receiveSNSId; // sns id 받을 건지
    private boolean receiveMoney; // 예약금 받을 건지


    public static EventResponseDTO toDTO(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .image(event.getThumbImage())
                .receiveInfo(event.isReceiveInfo())
                .receiveName(event.isReceiveName())
                .receiveGender(event.isReceiveGender())
                .receivePhoneNumber(event.isReceivePhoneNumber())
                .receiveMoney(event.isReceiveMoney())
                .receiveSNSId(event.isReceiveSNSId())
                .build();
    }
}
