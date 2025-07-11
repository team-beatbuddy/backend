package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.event.dto.EventUpdateRequestDTO;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import com.ceos.beatbuddy.global.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private LocalDate startDate;
    private LocalDate endDate;
    private String location;

    private int entranceFee; // 입장료
    private String entranceNotice; // 입장료 공지
    private String notice;
    private boolean isFreeEntrance; // 무료 입장 여부

    private String thumbImage;

    @ElementCollection
    @Setter
    private List<String> imageUrls;

    private int views;
    private int likes;

    @Column(nullable = false)
    private boolean receiveInfo = false; // 참석자 정보 수집 여부
    @Column(nullable = false)
    private boolean receiveName = false; // 이름 받을 건지
    @Column(nullable = false)
    private boolean receiveGender = false; // 성별 받을 건지
    @Column(nullable = false)
    private boolean receivePhoneNumber= false; // 전화번호 받을 건지
    @Column(nullable = false)
    private boolean receiveTotalCount= false; // 동행 인원 받을 건지
    @Column(nullable = false)
    private boolean receiveSNSId= false; // sns id 받을 건지
    @Column(nullable = false)
    private boolean receiveMoney= false; // 예약금 받을 건지

    @Setter
    private String depositAccount; // 사전 예약금 계좌번호
    @Setter
    private Integer depositAmount; // 사전 예약금 금액

    @Column(nullable = false)
    private boolean isVisible = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;  // 비트버디 등록된 장소

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    public enum Region {
        홍대,
        강남_신사,
        압구정_로데오,
        이태원,
        기타
    }

    public static Region of(String value) {
        try {
            return Region.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(EventErrorCode.REGION_NOT_EXIST);
        }
    }

    public void setThumbImage(String imageUrl) {
        this.thumbImage = imageUrl;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void updateEventInfo(EventUpdateRequestDTO dto) {
        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            this.title = dto.getTitle();
        }
        if (dto.getContent() != null) {
            this.content = dto.getContent();
        }
        if (dto.getStartDate() != null) {
            this.startDate = dto.getStartDate();
        }
        if (dto.getEndDate() != null) {
            this.endDate = dto.getEndDate();
        }
        if (dto.getLocation() != null) {
            this.location = dto.getLocation();
        }
        if (dto.getIsVisible() != null) {
            this.isVisible = dto.getIsVisible();
        }
        if (dto.getEntranceFee() != null) {
            this.entranceFee = dto.getEntranceFee();
        }
        if (dto.getEntranceNotice() != null && !dto.getEntranceNotice().trim().isEmpty()) {
            this.entranceNotice = dto.getEntranceNotice();
        }
        if (dto.getIsFreeEntrance() != null) {
            this.isFreeEntrance = dto.getIsFreeEntrance();
        }
        if (dto.getNotice() != null && !dto.getNotice().trim().isEmpty()) {
            this.notice = dto.getNotice();
        }
        if (dto.getRegion() != null) {
            try {
                this.region = Region.valueOf(dto.getRegion());
            } catch (IllegalArgumentException e) {
                throw new CustomException(EventErrorCode.REGION_NOT_EXIST);
            }
        }
    }

    public void updateReceiveSettings(Boolean receiveInfo,
                                      Boolean receiveName,
                                      Boolean receiveGender,
                                      Boolean receivePhoneNumber,
                                      Boolean receiveTotalCount,
                                      Boolean receiveSNSId) {
        if (receiveInfo != null) {
            this.receiveInfo = receiveInfo;
            if (!receiveInfo) {
                this.receiveName = false;
                this.receiveGender = false;
                this.receivePhoneNumber = false;
                this.receiveTotalCount = false;
                this.receiveSNSId = false;
                return;
            }
        }
        if (Boolean.TRUE.equals(this.receiveInfo)) {
            if (receiveName != null) this.receiveName = receiveName;
            if (receiveGender != null) this.receiveGender = receiveGender;
            if (receivePhoneNumber != null) this.receivePhoneNumber = receivePhoneNumber;
            if (receiveTotalCount != null) this.receiveTotalCount = receiveTotalCount;
            if (receiveSNSId != null) this.receiveSNSId = receiveSNSId;
        }
    }

    public void updateDepositSettings(Boolean receiveMoney, String depositAccount, Integer depositAmount) {
        if (receiveMoney != null) this.receiveMoney = receiveMoney;

        boolean finalReceiveMoney = receiveMoney != null ? receiveMoney : this.receiveMoney;

        if (finalReceiveMoney) {
            if (depositAccount != null) this.depositAccount = depositAccount;
            if (depositAmount != null) this.depositAmount = depositAmount;
        } else {
            this.depositAccount = null;
            this.depositAmount = null;
        }
    }
}
