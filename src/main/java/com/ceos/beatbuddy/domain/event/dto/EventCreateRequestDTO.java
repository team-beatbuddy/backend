package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateRequestDTO {

    @Schema(description = "이벤트 제목")
    private String title;

    @Schema(description = "이벤트 소개/설명")
    private String content;

    @Schema(description = "이벤트 시작 날짜 (yyyy-MM-dd)")
    private LocalDate startDate;

    @Schema(description = "이벤트 종료 날짜 (yyyy-MM-dd)")
    private LocalDate endDate;

    @Schema(description = "이벤트 장소 주소")
    private String location;

    @Schema(description = "참석자 정보 수집 여부 (true면 수집)")
    private boolean receiveInfo;

    @Schema(description = "연결된 비트버디 Venue ID (없으면 null), 리스트로 넣어서 그 안에서 검색?")
    private Long venueId;
    
    @Schema(description = "국민 XXXXXXXXX (한칸만 띄어서 전체 다 주시면 됩니다.")
    private String depositAccount; // 사전 예약금 계좌번호
    
    @Schema(description = "예약 금액")
    private Integer depositAmount; // 사전 예약금 금액


    public static Event toEntity(EventCreateRequestDTO eventCreateRequestDTO, Member member) {
        return Event.builder()
                .host(member)
                .title(eventCreateRequestDTO.getTitle())
                .content(eventCreateRequestDTO.getContent())
                .likes(0)
                .isVisible(true)
                .views(0)
                .scraps(null)
                .receiveInfo(eventCreateRequestDTO.receiveInfo)
                .depositAccount(eventCreateRequestDTO.getDepositAccount())
                .depositAmount(eventCreateRequestDTO.getDepositAmount())
                .location(eventCreateRequestDTO.getLocation())
                .startDate(eventCreateRequestDTO.getStartDate())
                .endDate(eventCreateRequestDTO.getEndDate())
                .build();
    }
}