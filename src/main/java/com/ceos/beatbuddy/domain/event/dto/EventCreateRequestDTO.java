package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateRequestDTO {

    @Schema(description = "이벤트 제목", example = "이벤트 제목")
    @NotNull(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "이벤트 소개/설명", example = "내용입니다.")
    @NotNull(message = "본문은 필수입니다.")
    private String content;

    @Schema(description = "이벤트 시작 날짜 (yyyy-MM-dd)", example = "2025-08-22")
    @NotNull(message = "시작날짜는 필수입니다.")
    private LocalDate startDate;

    @Schema(description = "이벤트 종료 날짜 (yyyy-MM-dd)", example = "2025-09-21")
    @NotNull(message = "종료날짜는 필수입니다.")
    private LocalDate endDate;

    @Schema(description = "이벤트 장소 주소", example = "서울시 강남구 테헤란로 123, venue 를 고르셨다면 해당 장소의 주소를 넣어주세요.")
    @NotNull(message = "장소는 필수입니다.")
    private String location;

    @Schema(description = "티켓 가격 (0이면 무료)", example = "20000")
    private int entranceFee; // 입장료
    @Schema(description = "입장료 공지", example = "입장료는 현장에서 결제해주세요.")
    private String entranceNotice; // 입장료 공지
    @Schema(description = "이벤트 공지사항", example = "이벤트 관련 공지사항입니다.")
    private String notice;
    @Schema(description = "무료 입장 여부 (true면 무료)", example = "false")
    private boolean isFreeEntrance; // 무료 입장 여부

    @Schema(description = "참석자 정보 수집 여부 (true면 수집)", example = "true")
    @NotNull(message = "참석자 정보 수집 여부는 필수입니다.")
    private boolean receiveInfo;
    @Schema(description = "참석자 이름 수집 여부 (true면 수집)", example = "true")
    private boolean receiveName; // 이름 받을 건지
    @Schema(description = "참석자 성별 수집 여부 (true면 수집)", example = "true")
    private boolean receiveGender; // 성별 받을 건지
    @Schema(description = "참석자 전화번호 수집 여부 (true면 수집)", example = "true")
    private boolean receivePhoneNumber; // 전화번호 받을 건지
    @Schema(description = "참석자 동행인원 수집 여부 (true면 수집)", example = "true")
    private boolean receiveTotalCount; // 동행 인원 받을 건지
    @Schema(description = "참석자 SNS ID 수집 여부 (true면 수집)", example = "true")
    private boolean receiveSNSId; // sns id 받을 건지
    @Schema(description = "참석자 예약금 수집 여부 (true면 수집)", example = "true")
    private boolean receiveMoney; // 예약금 받을 건지

    @Schema(description = "연결된 비트버디 Venue ID (없으면 null), 리스트로 넣어서 그 안에서 검색?", example = "1")
    private Long venueId;
    
    @Schema(description = "국민 XXXXXXXXX (한칸만 띄어서 전체 다 주시면 됩니다.", example = "국민 122235-455678952")
    private String depositAccount; // 사전 예약금 계좌번호
    
    @Schema(description = "예약 금액", example = "20000")
    private Integer depositAmount; // 사전 예약금 금액

    @Schema(description = "이벤트 지역 (예: 홍대, 강남_신사 등)", example = "홍대")
    @NotNull(message = "지역은 필수입니다.")
    private String region;


    public static Event toEntity(EventCreateRequestDTO eventCreateRequestDTO, Member member) {
        return Event.builder()
                .host(member)
                .title(eventCreateRequestDTO.getTitle())
                .content(eventCreateRequestDTO.getContent())
                .likes(0)
                .isVisible(true)
                .views(0)
                .entranceFee(eventCreateRequestDTO.getEntranceFee())
                .entranceNotice(eventCreateRequestDTO.getEntranceNotice())
                .notice(eventCreateRequestDTO.getNotice())
                .isFreeEntrance(eventCreateRequestDTO.isFreeEntrance())
                .receiveInfo(eventCreateRequestDTO.isReceiveInfo())
                .depositAccount(eventCreateRequestDTO.getDepositAccount())
                .depositAmount(eventCreateRequestDTO.getDepositAmount())
                .receiveName(eventCreateRequestDTO.isReceiveName())
                .receiveGender(eventCreateRequestDTO.isReceiveGender())
                .receiveSNSId(eventCreateRequestDTO.isReceiveSNSId())
                .receivePhoneNumber(eventCreateRequestDTO.isReceivePhoneNumber())
                .receiveTotalCount(eventCreateRequestDTO.isReceiveTotalCount())
                .receiveMoney(eventCreateRequestDTO.isReceiveMoney())
                .location(eventCreateRequestDTO.getLocation())
                .startDate(eventCreateRequestDTO.getStartDate())
                .endDate(eventCreateRequestDTO.getEndDate())
                .region(Event.of(eventCreateRequestDTO.getRegion()))
                .build();
    }
}